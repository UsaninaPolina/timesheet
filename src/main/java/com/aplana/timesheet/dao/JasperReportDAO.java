package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.reports.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.HibernateQueryResultDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class JasperReportDAO {

    private static final Logger logger = LoggerFactory.getLogger(JasperReportDAO.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport01Data(Report01 report) {
		String workDaySeparator;
		if(OverTimeCategory.Holiday.equals(report.getCategory())) {
			workDaySeparator="and h.calDate is not null ";
		} else if(OverTimeCategory.Simple.equals(report.getCategory())) {
			workDaySeparator="and h.calDate is null ";
		} else {
			workDaySeparator = "";
		}
		boolean hasRegion;
		String regionClause;
		if (report.getRegionId() != null && !report.getRegionId().equals(0)) {
			regionClause = "em.region.id = :regionId and ";
			hasRegion = true;
		} else {
			regionClause = "";
			hasRegion = false;
		}
        Query query = entityManager.createQuery(
                "select em.id, em.name, ts.calDate.calDate, cast('' as string), " +
                        "sum(td.duration)-8, sum(td.duration), h.id, h.region.id, " +
                        "(case when h is not null then td.project.name else cast('%NO_GROUPING%' as string) end), " +
                        "(case when h is not null then td.duration else cast(-1 as float) end) " +
                        "from TimeSheetDetail td " +
                        "inner join td.timeSheet ts " +
                        "inner join ts.employee em " +
                        "left outer join ts.calDate.holidays h " +
                        "where em.division.id = :divisionId and " +
						regionClause +
                        "ts.calDate.calDate between :beginDate and :endDate " +
                        "and ((h.region.id is null) or (h.region.id=em.region.id)) " +
						workDaySeparator +
                        "group by em.id, em.name, ts.calDate.calDate, h, h.region.id, 9, 10 " +
                        "having (sum(td.duration) > 8) or (h is not null)" +
                        "order by em.name, h.id desc, ts.calDate.calDate");

        if (hasRegion) {
            query.setParameter("regionId", report.getRegionId());
		}
        query.setParameter("divisionId", report.getDivisionId());
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List resultList = query.getResultList();

        // Запрос достанет для сотрудников наименования проектов по датам
        Query projQuery = entityManager.createQuery("select em.id, ts.calDate.calDate, td.project.name " +
                "from TimeSheetDetail td " +
                "inner join td.timeSheet ts " +
                "inner join ts.employee em " +
                "where em.division.id = :divisionId and " +
                (!report.getRegionId().equals(0) ? "em.region.id = :regionId and " : "") +
                "ts.calDate.calDate between :beginDate and :endDate ");

        if (hasRegion)
            projQuery.setParameter("regionId", report.getRegionId());
        projQuery.setParameter("divisionId", report.getDivisionId());
        projQuery.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        projQuery.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List projResultList = projQuery.getResultList();

        String projNames = "";

        List<String> projNamesList;

        // Пробежим весь список отчетов и заполним в них списки проектов, над которыми работали сотрудники
        for (Iterator iterator = resultList.iterator(); iterator.hasNext(); ) {
            Object[] next = (Object[]) iterator.next();

            if (next[6] != null)
                continue;

            Integer emplId = (Integer) next[0];
            Timestamp date = (Timestamp) next[2];

            projNamesList = new ArrayList<String>();

            for (Iterator iterator1 = projResultList.iterator(); iterator1.hasNext(); ) {
                Object[] o = (Object[]) iterator1.next();

                if (o[0].equals(emplId) && o[1].equals(date)) {
                    if (!projNamesList.contains(o[2])) {
                        projNamesList.add((String) o[2]);
                    }
                }
            }

            projNames = "";

            for (Iterator<String> namesIterator = projNamesList.iterator(); namesIterator.hasNext(); ) {
                String nextName = namesIterator.next();

                projNames += nextName;
                if (namesIterator.hasNext())
                    projNames += ", ";
            }

            next[3] = projNames;

        }

        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[]{"id", "name", "caldate", "projnames", "overtime", "duration", "holiday", "region", "projdetail", "durationdetail"};
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport02Data(Report02 report) {
        Query query = null;
		boolean hasProject;
		
		if (report.getProjectId() != null && report.getProjectId() != 0) {
			hasProject = true;
		} else {
			hasProject = false;
		}

        if (hasProject) {
            // Выборка по конкретному проекту
            query = entityManager.createQuery("SELECT empl.name, d.name, p.name, tsd.cqId, sum(tsd.duration), " +
                    "(case when h is null then 0 " +
                    "else " +
                    "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                    "else 1 end end),  max(h.region.id) " +
                    "FROM TimeSheetDetail tsd " +
                    "join tsd.timeSheet ts " +
                    "join ts.employee empl " +
                    "join ts.calDate c " +
                    "join empl.division d " +
                    "join tsd.project p " +
                    "left outer join c.holidays h " +
                    "WHERE " +
                    "tsd.duration > 0 AND " +
                    "tsd.project.id=:projectId AND " +
                    (report.getEmployeeId() == null ? "" : "empl.id=:emplId AND ") +
                    (report.getEmplDivisionId() == null ? "" : "d.id=:emplDivisionId AND ") +
                    (report.getRegionId()!=null ? "empl.region.id = :regionId AND " : "") +
                    "c.calDate between :beginDate AND :endDate " +
                    "GROUP BY empl.name, d.name, p.name, tsd.cqId, 6 " +
                    "ORDER BY empl.name, p.name, tsd.cqId ");

            query.setParameter("projectId", report.getProjectId());

        } else {
            if (report.getFilterProjects()) {
                // Выборка по всем проектам центра
                query = entityManager.createQuery("SELECT empl.name, d.name, p.name, tsd.cqId, sum(tsd.duration), " +
                        "(case when h is null then 0 " +
                        "else " +
                        "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                        "else 1 end end), max(h.region.id) " +
                        "FROM TimeSheetDetail tsd " +
                        "join tsd.timeSheet ts " +
                        "join ts.employee empl " +
                        "join ts.calDate c " +
                        "join empl.division d " +
                        "join tsd.project p " +
                        "join p.divisions dp " +
                        "left outer join c.holidays h " +
                        "WHERE " +
                        "tsd.duration > 0 AND " +
                        "dp.id=:divisionId AND " +
                        (report.getEmployeeId() == null ? "" : "empl.id=:emplId AND ") +
                        (report.getEmplDivisionId() == null ? "" : "d.id=:emplDivisionId AND ") +
                        (report.getRegionId() != null ? "empl.region.id = :regionId AND " : "") +
                        "c.calDate between :beginDate AND :endDate " +
                        "GROUP BY empl.name, d.name, p.name, tsd.cqId, 6 " +
                        "ORDER BY empl.name, p.name, tsd.cqId ");

                query.setParameter("divisionId", report.getDivisionId());

            } else {

                // Выборка по всем проектам всех центров
                query = entityManager.createQuery("SELECT empl.name, d.name, p.name, tsd.cqId, sum(tsd.duration), " +
                        "(case when h is null then 0 " +
                        "else " +
                        "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                        "else 1 end end), max(h.region.id) " +
                        "FROM TimeSheetDetail tsd " +
                        "join tsd.timeSheet ts " +
                        "join ts.employee empl " +
                        "join ts.calDate c " +
                        "join empl.division d " +
                        "join tsd.project p " +
                        "left outer join c.holidays h " +
                        "WHERE " +
                        "tsd.duration > 0 AND " +
                        (report.getEmployeeId() == null ? "" : "empl.id=:emplId AND ") +
                        (report.getEmplDivisionId() == null ? "" : "d.id=:emplDivisionId AND ") +
                        (report.getRegionId() != null ? "empl.region.id = :regionId AND " : "") +
                        "c.calDate between :beginDate AND :endDate " +
                        "GROUP BY empl.name, d.name, p.name, tsd.cqId, 6 " +
                        "ORDER BY empl.name, p.name, tsd.cqId ");
            }
        }
        if (report.getRegionId() != null && !report.getRegionId().equals(0))
            query.setParameter("regionId", report.getRegionId());
        if (report.getEmployeeId() != null && report.getEmployeeId() != 0)
            query.setParameter("emplId", report.getEmployeeId());
        if (report.getEmplDivisionId() != null && report.getEmplDivisionId() != 0)
            query.setParameter("emplDivisionId", report.getEmplDivisionId());


        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List resultList = query.getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[] { "name", "empldivision", "project",
                    "taskname", "duration", "holiday", "region" };
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport03Data(Report03 report) {

        Query query = null;
		boolean hasProject;
		
		if (report.getProjectId() != null && report.getProjectId() != 0) {
			hasProject = true;
		} else {
			hasProject = false;
		}
		
        if (hasProject) {

            // Выборка по конкретному проекту
            query = entityManager.createQuery("SELECT empl.name, d.name, p.name, tsd.cqId, c.calDate, sum(tsd.duration), " +
                    "(case when h is null then 0 " +
                    "else " +
                    "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                    "else 1 end end), h.region.id " +
                    "FROM TimeSheetDetail tsd " +
                    "join tsd.timeSheet ts " +
                    "join ts.employee empl " +
                    "join ts.calDate c " +
                    "join empl.division d " +
                    "join tsd.project p " +
                    "left outer join c.holidays h " +
                    "WHERE " +
                    "tsd.duration > 0 AND " +
                    "tsd.project.id=:projectId AND " +
                    (report.getEmployeeId() == null ? "" : "empl.id=:emplId AND ") +
                    (report.getEmplDivisionId() == null ? "" : "d.id=:emplDivisionId AND ") +
                    (report.getRegionId() != null ? "empl.region.id = :regionId AND " : "") +
                    "c.calDate between :beginDate AND :endDate " +
                    "GROUP BY empl.name, d.name, p.name, tsd.cqId, c.calDate, h.id, h.region.id, empl.region.id " +
                    "ORDER BY empl.name, p.name, tsd.cqId, c.calDate ");

			query.setParameter("projectId", report.getProjectId());


        } else {
            if (report.getFilterProjects()) {
                // Выборка по всем проектам центра
                query = entityManager.createQuery("SELECT empl.name, d.name, p.name, tsd.cqId, c.calDate, sum(tsd.duration), " +
                        "(case when h is null then 0 " +
                        "else " +
                        "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                        "else 1 end end), h.region.id " +
                        "FROM TimeSheetDetail tsd " +
                        "join tsd.timeSheet ts " +
                        "join ts.employee empl " +
                        "join ts.calDate c " +
                        "join empl.division d " +
                        "join tsd.project p " +
                        "join p.divisions dp " +
                        "left outer join c.holidays h " +
                        "WHERE " +
                        "tsd.duration > 0 AND " +
                        "dp.id=:divisionId AND " +
                        (report.getEmployeeId() == null ? "" : "empl.id=:emplId AND ") +
                        (report.getEmplDivisionId() == null ? "" : "d.id=:emplDivisionId AND ") +
                        (report.getRegionId() == null ? "empl.region.id = :regionId AND " : "") +
                        "c.calDate between :beginDate AND :endDate " +
                        "GROUP BY empl.name, d.name, p.name, tsd.cqId, c.calDate, h.id, h.region.id, empl.region.id " +
                        "ORDER BY empl.name, p.name, tsd.cqId, c.calDate ");

                query.setParameter("divisionId", report.getDivisionId());

            } else {

                // Выборка по всем проектам всех центров
                query = entityManager.createQuery("SELECT empl.name, d.name, p.name, tsd.cqId, c.calDate, sum(tsd.duration), " +
                        "(case when h is null then 0 " +
                        "else " +
                        "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                        "else 1 end end), h.region.id " +
                        "FROM TimeSheetDetail tsd " +
                        "join tsd.timeSheet ts " +
                        "join ts.employee empl " +
                        "join ts.calDate c " +
                        "join empl.division d " +
                        "join tsd.project p " +
                        "left outer join c.holidays h " +
                        "WHERE " +
                        "tsd.duration > 0 AND " +
                        (report.getEmployeeId() == null ? "" : "empl.id=:emplId AND ") +
                        (report.getEmplDivisionId() == null ? "" : "d.id=:emplDivisionId AND ") +
                        (report.getRegionId() != null ? "empl.region.id = :regionId AND " : "") +
                        "c.calDate between :beginDate AND :endDate " +
                        "GROUP BY empl.name, d.name, p.name, tsd.cqId, c.calDate, h.id, h.region.id, empl.region.id " +
                        "ORDER BY empl.name, p.name, tsd.cqId, c.calDate ");
            }
        }
        if (report.getRegionId() != null && report.getRegionId() != 0)
            query.setParameter("regionId", report.getRegionId());
        if (report.getEmployeeId() != null && report.getEmployeeId() != 0)
            query.setParameter("emplId", report.getEmployeeId());
        if (report.getEmplDivisionId() != null && report.getEmplDivisionId() != 0)
            query.setParameter("emplDivisionId", report.getEmplDivisionId());

        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List resultList = query.getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[]{"name", "empldivision", "project", "taskname", "caldate", "duration", "holiday", "region"};
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }

    }

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport04Data(Report04 report) {
		
		boolean hasRegion;
		String regionClause;
		if (report.getRegionId() != null && !report.getRegionId().equals(0)) {
			regionClause = "and emp.region.id = :regionId ";
			hasRegion = true;
		} else {
			regionClause = "";
			hasRegion = false;
		}
		
        Query query = entityManager.createQuery("select c.calDate, emp.name " +
                "from Calendar c, Employee emp " +
                "where c.calDate between :beginDate and " +
                "(CASE  " +
                "WHEN emp.endDate IS NOT NULL THEN " +
                    "CASE  " +
                    "WHEN emp.endDate < :endDate THEN emp.endDate " +
                    "ELSE :endDate " +
                    "END " +
                "ELSE :endDate " +
                "END) " +
                "and emp.division.id = :divisionId " +
                 regionClause +
                "and (emp.notToSync = false) " +
                "and emp.manager.id is not null " +
                "and c.calDate not in (select ts.calDate.calDate " + //note: date don't have report
                "from TimeSheet ts " +
                "where ts.employee.id = emp.id) " +
                "and c.calDate not in (select h.calDate.calDate " +  //note: date is not holiday in employee's region
                "from Holiday h " +
                "where h.region.id is null " +
                "or  h.region.id = emp.region.id ) " +
                "and (emp.startDate <= c.calDate) " +
                "order by emp.name, c.calDate");

        if (hasRegion)
            query.setParameter("regionId", report.getRegionId());
        query.setParameter("divisionId", report.getDivisionId());
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List resultList = query.getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[]{"date", "name"};
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport05Data(Report05 report) {
		boolean hasRegion;
		String regionClause;
		boolean hasEmployee;
		String employeeClause;
		
		if (report.getRegionId() != null && report.getRegionId() != 0) {
			hasRegion = true;
			regionClause = "td.timeSheet.employee.region.id = :regionId and ";
		} else {
			hasRegion = false;
			regionClause = "";
		}
		
		if (report.getEmployeeId() != null && report.getEmployeeId() != 0) {
			hasEmployee = true;
			employeeClause = "em.id = :employeeId and ";
		} else {
			hasEmployee = false;
			employeeClause = "";
		}
				
        Query query = entityManager.createQuery(
                "select ts.calDate.calDate, em.name, td.actType.value, td.project.name, td.actCat.value, em.job.name, " +
                "COALESCE(td.cqId, ''), td.duration, td.description, td.problem " +
                "from TimeSheetDetail td " +
                "inner join td.timeSheet ts " +
                "inner join ts.employee em " +
                "where td.timeSheet.employee.division.id = :divisionId and " +
                regionClause +
				employeeClause +
                "td.timeSheet.calDate.calDate between :beginDate and :endDate " +
                "order by td.timeSheet.employee.name,td.timeSheet.calDate.calDate");

        if (hasRegion)
            query.setParameter("regionId", report.getRegionId());
        if (hasEmployee)
            query.setParameter("employeeId", report.getEmployeeId());
        query.setParameter("divisionId", report.getDivisionId());
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));


        List resultList = query.getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[]{"calDate", "name", "value", "pctName", "actType",
                    "pctRole", "taskName", "duration", "description", "problem"};
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport06Data(Report06 report) {
		
		boolean hasRegion;
		String regionClause;
		if (report.getRegionId() != null && !report.getRegionId().equals(0)) {
			regionClause = "tsd.timeSheet.employee.region.id = :regionId AND ";
			hasRegion = true;
		} else {
			regionClause = "";
			hasRegion = false;
		}
		
        Query query = entityManager.createQuery(
                "SELECT sum(tsd.duration), act.projectRole.name, tsd.timeSheet.employee.name, tsd.actCat.value " +
                        "FROM TimeSheetDetail tsd, AvailableActivityCategory act " +
                        "WHERE tsd.project.id=:projectId AND tsd.actType=act.actType AND tsd.actCat=act.actCat AND " +
                        regionClause +
                        "tsd.timeSheet.calDate.calDate between :beginDate AND :endDate AND act.projectRole=tsd.timeSheet.employee.job " +
                        "GROUP BY act.projectRole.name, tsd.timeSheet.employee.name, tsd.actCat.value " +
                        "ORDER BY tsd.timeSheet.employee.name asc");

        if (hasRegion)
            query.setParameter("regionId", report.getRegionId());
        query.setParameter("projectId", report.getProjectId());
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List resultList = query.getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[]{"duration", "act_type", "name", "act_cat"};
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }

    }
}
