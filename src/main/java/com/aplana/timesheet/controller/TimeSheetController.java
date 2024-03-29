package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.form.validator.TimeSheetFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EmployeeHelper;
import com.aplana.timesheet.util.TimeSheetUser;
import com.aplana.timesheet.util.ViewReportHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class TimeSheetController {
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetController.class);

    @Autowired
    private DivisionService divisionService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    ProjectParticipantService projectParticipantService;
    @Autowired
    private ProjectTaskService projectTaskService;
    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private TimeSheetService timeSheetService;
    @Autowired
    private AvailableActivityCategoryService availableActivityCategoryService;
    @Autowired
    TimeSheetFormValidator tsFormValidator;
    @Autowired
    SendMailService sendMailService;
    @Autowired
    EmployeeLdapService employeeLdapService;
    @Autowired
    SecurityService securityService;
	@Autowired
	EmployeeHelper employeeHelper;
    @Autowired
    ViewReportHelper viewReportHelper;

    @RequestMapping(value = "/timesheet", method = RequestMethod.GET)
    public ModelAndView showMainForm(@RequestParam(value = "date",required = false) String date,
                                     @RequestParam(value = "id",required = false) Integer id) {
        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        logger.info("Showing Time Sheet main page!");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("timesheet");

        TimeSheetForm tsForm = new TimeSheetForm();

        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser != null) {
            tsForm.setDivisionId(securityUser.getEmployee().getDivision().getId());
            tsForm.setEmployeeId(securityUser.getEmployee().getId());
        }
        if (id!=null) {
            tsForm.setDivisionId(employeeService.find(id).getDivision().getId());
            tsForm.setEmployeeId(id);
        }
        if (date != null) {
            DateTimeUtil.stringToDate(date, "dd.MM.yyyy");
            tsForm.setCalDate(date);
            mav.addObject("selectedCalDateJson", getSelectedCalDateJson(tsForm));   //выставляем дату для DateTextBox
        } else {
            mav.addObject("selectedCalDateJson", "''");
        }
        mav.addObject("timeSheetForm", tsForm); // command object
        mav.addObject("selectedProjectRolesJson", "[{row:'0', role:''}]");
        mav.addObject("selectedProjectTasksJson", "[{row:'0', task:''}]");
        mav.addObject("selectedProjectsJson", "[{row:'0', project:''}]");
        mav.addObject("selectedActCategoriesJson", "[{row:'0', actCat:''}]");
        mav.addObject("selectedLongVacationIllnessJson", getSelectedLongVacationIllnessJson(tsForm));

        //mav.addObject("rows_am",am_string>7 ? 7 :am_string);
        mav.addAllObjects(getListsToMAV());
        return mav;
    }

    @RequestMapping(value = "/cqcodes", method = RequestMethod.GET)
    public String showCqCodes() {
        logger.info("Showing CQ Codes page!");
        return "cqcodes";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "redirect:timesheet";
    }

    @RequestMapping(value = "/newReport", method = RequestMethod.POST)
    public String newReport() {
        return "redirect:timesheet";
    }

    //Пользователь нажал на кнопку "Отправить новый отчёт" на странице selected.jsp
    @RequestMapping(value = "/sendNewReport", method = RequestMethod.POST)
    public String sendNewReport() {
        return "redirect:timesheet";
    }


   // @RequestMapping(value="/getPlan", method = RequestMethod.POST)
   // public ModelAndView getPlan()       {
        //ModelAndView mav
    //    return  ModelAndView mavWithErrors = new ModelAndView("timesheet");
    //}

    @RequestMapping(value = "/timesheet", method = RequestMethod.POST)
    public ModelAndView sendTimeSheet(@ModelAttribute("timeSheetForm") TimeSheetForm tsForm, BindingResult result) {
        logger.info("Processing form validation for employee {} ({}).", tsForm.getEmployeeId(), tsForm.getCalDate());
        tsFormValidator.validate(tsForm, result);
        if (result.hasErrors()) {
            logger.info("TimeSheetForm for employee {} has errors. Form not validated.", tsForm.getEmployeeId());
            ModelAndView mavWithErrors = new ModelAndView("timesheet");
            mavWithErrors.addObject("timeSheetForm", tsForm);
            mavWithErrors.addObject("errors", result.getAllErrors());
            mavWithErrors.addObject("selectedProjectsJson", getSelectedProjectsJson(tsForm));
            mavWithErrors.addObject("selectedProjectRolesJson", getSelectedProjectRolesJson(tsForm));
            mavWithErrors.addObject("selectedProjectTasksJson", getSelectedProjectTasksJson(tsForm));
            mavWithErrors.addObject("selectedActCategoriesJson", getSelectedActCategoriesJson(tsForm));
            mavWithErrors.addObject("selectedLongVacationIllnessJson", getSelectedLongVacationIllnessJson(tsForm));
            mavWithErrors.addObject("selectedCalDateJson", getSelectedCalDateJson(tsForm));
            mavWithErrors.addAllObjects(getListsToMAV());

            return mavWithErrors;
        }
        timeSheetService.storeTimeSheet(tsForm);
        sendMailService.performMailing(tsForm);

        ModelAndView mav = new ModelAndView("selected");
        mav.addObject("timeSheetForm", tsForm);
        logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        return mav;
    }

    //Пользователь списывает занятость за продолжительные отпуск или болезнь.
    @RequestMapping(value = "/timesheetLong", method = RequestMethod.POST)
    public ModelAndView sendTimeSheetLong(@ModelAttribute("timeSheetForm") TimeSheetForm tsForm, BindingResult result) {
        logger.info("Processing form validation for employee {} ({}) (long).", tsForm.getEmployeeId(), tsForm.getCalDate());
        tsFormValidator.validate(tsForm, result);
        if (result.hasErrors()) {
            logger.info("TimeSheetForm for employee {} has errors. Form not validated (long).", tsForm.getEmployeeId());
            ModelAndView mavWithErrors = new ModelAndView("timesheet");
            mavWithErrors.addObject("timeSheetForm", tsForm);
            mavWithErrors.addObject("errors", result.getAllErrors());
            mavWithErrors.addObject("selectedProjectRolesJson", "[{row:'0', role:''}]");
            mavWithErrors.addObject("selectedProjectTasksJson", "[{row:'0', task:''}]");
            mavWithErrors.addObject("selectedProjectsJson", "[{row:'0', project:''}]");
            mavWithErrors.addObject("selectedActCategoriesJson", "[{row:'0', actCat:''}]");
            mavWithErrors.addObject("selectedCalDateJson", "''");
            mavWithErrors.addObject("selectedLongVacationIllnessJson", getSelectedLongVacationIllnessJson(tsForm));
            mavWithErrors.addAllObjects(getListsToMAV());

            return mavWithErrors;
        }
        timeSheetService.storeTimeSheetLong(tsForm);
        sendMailService.performMailing(tsForm);

        ModelAndView mav = new ModelAndView("selected");
        mav.addObject("timeSheetForm", tsForm);
        logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        return mav;
    }

    /**
     * Удаляет отчет по id. В случае если текущий авторизованный пользователь является руководителем сотрудника, добавившего отчет.
     *
     * @param id
     * @return OK или Error
     */
    @RequestMapping(value = "/timesheetDel/{id}", method = RequestMethod.POST)
    public String delTimeSheet(@PathVariable("id") Integer id, HttpServletRequest httpRequest) {
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser == null) {
            throw new SecurityException("Не найден пользователь в контексте безопасности.");
        }

        TimeSheet timeSheet = timeSheetService.find(id);

        logger.info("Удаляется отчет " + timeSheet + ". Инициатор: " + securityUser.getEmployee().getName());
        timeSheetService.delete(timeSheet);

        sendMailService.performTimeSheetDeletedMailing(timeSheet);

        return "redirect:" + httpRequest.getHeader("Referer");
    }

    /*
      * Возвращает HashMap со значениями для заполнения списков сотрудников,
      * проектов, пресейлов, проектных задач, типов и категорий активности на
      * форме приложения.
      */
    private Map<String, Object> getListsToMAV() {
        Map<String, Object> result = new HashMap<String, Object>();
        List<DictionaryItem> typesOfActivity = dictionaryItemService.getTypesOfActivity();
        StringBuilder typesOfActivityJson = new StringBuilder();
        typesOfActivityJson.append("[");
        for (DictionaryItem item : typesOfActivity) {
            typesOfActivityJson.append("{id:'");
            typesOfActivityJson.append(item.getId().toString());
            typesOfActivityJson.append("', value:'");
            typesOfActivityJson.append(item.getValue());
            typesOfActivityJson.append("'},");
        }
        result.put("actTypeList", typesOfActivity);
        result.put("actTypeJson", typesOfActivityJson.toString().substring(0, (typesOfActivityJson.toString().length() - 1)) + "]");

        List<Division> divisions = divisionService.getDivisions();
        result.put("divisionList", divisions);

        result.put("employeeListJson", employeeHelper.getEmployeeListJson(divisions));

        List<DictionaryItem> categoryOfActivity = dictionaryItemService.getCategoryOfActivity();
        StringBuilder actCategoryListJson = new StringBuilder();
        actCategoryListJson.append("[");
        for (DictionaryItem item : categoryOfActivity) {
            actCategoryListJson.append("{id:'");
            actCategoryListJson.append(item.getId().toString());
            actCategoryListJson.append("', value:'");
            actCategoryListJson.append(item.getValue());
            actCategoryListJson.append("'},");
        }
        result.put("actCategoryList", categoryOfActivity);
        result.put("actCategoryListJson", actCategoryListJson.toString().substring(0, (actCategoryListJson.toString().length() - 1)) + "]");
        result.put("availableActCategoriesJson", getAvailableActCategoriesJson());

        result.put("projectListJson", projectService.getProjectListJson(divisions));
        result.put("projectTaskListJson", getProjectTaskListJson(projectService.getProjectsWithCq()));

        List<ProjectRole> projectRoleList = projectRoleService.getProjectRoles();

        for (int i = 0; i < projectRoleList.size(); i++) {
            if (projectRoleList.get(i).getCode().equals("ND")) {  // Убираем из списка роль "Не определена" APLANATS-270
                projectRoleList.remove(i);
                break;
            }
        }

        result.put("projectRoleList", projectRoleList);
        StringBuilder projectRoleListJson = new StringBuilder();
        projectRoleListJson.append("[");
        for (ProjectRole item : projectRoleList) {
            projectRoleListJson.append("{id:'");
            projectRoleListJson.append(item.getId().toString());
            projectRoleListJson.append("', value:'");
            projectRoleListJson.append(item.getName());
            projectRoleListJson.append("'},");
        }
        result.put("projectRoleListJson", projectRoleListJson.toString().substring(0, (projectRoleListJson.toString().length() - 1)) + "]");

        return result;
    }

    private String getProjectTaskListJson(List<Project> projects) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < projects.size(); i++) {
            Integer projectId = projects.get(i).getId();
            List<ProjectTask> tasks = projectTaskService.getProjectTasks(projectId);
            sb.append("{projId:'");
            sb.append(projectId);
            sb.append("', projTasks:[");
            for (int j = 0; j < tasks.size(); j++) {
                sb.append("{id:'");
                sb.append(tasks.get(j).getCqId());
                sb.append("', value:'");
                sb.append(tasks.get(j).getCqId());
                sb.append("'}");
                if (j < (tasks.size() - 1)) {
                    sb.append(", ");
                }
            }
            sb.append("]}");
            if (i < (projects.size() - 1)) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String getSelectedProjectTasksJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                if (!"".equals(tablePart.get(i).getCqId())) {
                    sb.append("{row:'").append(i).append("', ");
                    sb.append("task:'").append(tablePart.get(i).getCqId()).append("'}");
                    if (i < (tablePart.size() - 1)) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("]");
        } else {
            sb.append("[{row:'0', task:''}]");
        }
        return sb.toString();
    }

    private String getSelectedProjectRolesJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                if (!"".equals(tablePart.get(i).getCqId())) {
                    sb.append("{row:'").append(i).append("', ");
                    sb.append("role:'").append(tablePart.get(i).getProjectRoleId()).append("'}");
                    if (i < (tablePart.size() - 1)) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("]");
        } else {
            sb.append("[{row:'0', role:''}]");
        }
        return sb.toString();
    }

    private String getSelectedProjectsJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                sb.append("{row:'").append(i).append("', ");
                sb.append("project:'").append(tablePart.get(i).getProjectId()).append("'}");
                if (i < (tablePart.size() - 1)) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        } else {
            sb.append("[{row:'0', project:''}]");
        }
        return sb.toString();
    }

    private String getSelectedActCategoriesJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                sb.append("{row:'").append(i).append("', ");
                sb.append("actCat:'").append(tablePart.get(i).getActivityCategoryId()).append("'}");
                if (i < (tablePart.size() - 1)) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        } else {
            sb.append("[{row:'0', actCat:''}]");
        }
        return sb.toString();
    }

    private String getSelectedLongVacationIllnessJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        if (tsForm.isLongIllness() || tsForm.isLongVacation()) {
            sb.append("[");
            sb.append("{vacation:'").append(tsForm.isLongVacation()).append("', ");
            sb.append("illness:'").append(tsForm.isLongIllness()).append("', ");
            sb.append("beginDate:'").append(tsForm.getBeginLongDate()).append("', ");
            sb.append("endDate:'").append(tsForm.getEndLongDate()).append("'}");
            sb.append("]");
        } else {
            sb.append("[{vacation:'false', illness:'false', beginDate:'', endDate:''}]");
        }
        return sb.toString();
    }

    private String getAvailableActCategoriesJson() {
        StringBuilder sb = new StringBuilder();
        List<DictionaryItem> actTypes = dictionaryItemService.getTypesOfActivity();
        List<ProjectRole> projectRoles = projectRoleService.getProjectRoles();
        sb.append("[");
        for (int i = 0; i < actTypes.size(); i++) {
            for (int j = 0; j < projectRoles.size(); j++) {
                sb.append("{actType:'").append(actTypes.get(i).getId()).append("', ");
                sb.append("projRole:'").append(projectRoles.get(j).getId()).append("', ");
                List<AvailableActivityCategory> avActCats = availableActivityCategoryService
                        .getAvailableActivityCategories(actTypes.get(i), projectRoles.get(j));
                sb.append("avActCats:[");
                for (int k = 0; k < avActCats.size(); k++) {
                    sb.append("'").append(avActCats.get(k).getActCat().getId()).append("'");
                    if (k < (avActCats.size() - 1)) {
                        sb.append(", ");
                    }
                }
                sb.append("]}");
                if (i < (actTypes.size())) {
                    sb.append(", ");
                }
            }
        }
        String result = sb.toString().substring(0, (sb.toString().length() - 2)) + "]";
        return result;
    }

    private String getSelectedCalDateJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        String date = "";
        sb.append("'");
        if (DateTimeUtil.isDateValid(tsForm.getCalDate())){
            date = DateTimeUtil.formatDateString(tsForm.getCalDate());
            sb.append(date);
        }
        sb.append("'");
        logger.debug("SelectedCalDateJson = {}", date);
        return sb.toString();
    }

    @RequestMapping(value = "/timesheet/dates", headers = "Accept=application/json")
    public @ResponseBody String reportDates(@RequestParam("queryYear") Integer queryYear, @RequestParam("queryMonth") Integer queryMonth, @RequestParam("employeeId") Integer employeeId) {
            return viewReportHelper.getDateReportsListJson(queryYear, queryMonth, employeeId);
    }
    /**
     * Возвращает планы предыдущего дня и на следующего дня
     * @param date (2012-11-25)
     * @param employeeId (573)
     * @return Json String
     */
    @RequestMapping(value = "/timesheet/plans", headers = "Accept=application/json;Charset=UTF-8")
    @ResponseBody
    public String getPlans(@RequestParam("date") String date, @RequestParam("employeeId") Integer employeeId) {
        return timeSheetService.getPlansJson(date, employeeId);
    }
}