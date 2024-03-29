package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class DictionaryItemDAO {
	public static final Integer PROJECTS_ID = 12;
	public static final Integer PRESALES_ID = 13;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DictionaryDAO dictionaryDAO;

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<DictionaryItem> getCategoryOfActivity() {
		Query query = entityManager
			.createQuery("from DictionaryItem as di where di.dictionary = :dictionary order by di.value desc");
		query.setParameter("dictionary", dictionaryDAO.find(DictionaryDAO.CATEGORY_OF_ACTIVITY_ID));
		List<DictionaryItem> result = query.getResultList();
		return result;
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<DictionaryItem> getTypesOfActivity() {
		Query query = entityManager
			.createQuery("from DictionaryItem as di where di.dictionary = :dictionary order by di.value desc");
		query.setParameter("dictionary", dictionaryDAO.find(DictionaryDAO.TYPES_OF_ACTIVITY_ID));
		List<DictionaryItem> result = query.getResultList();
		return result;
	}

	@Transactional(readOnly = true)
	public DictionaryItem find(Integer id) {
		DictionaryItem dictionaryItem = entityManager.find(DictionaryItem.class, id);
		return dictionaryItem;
	}
}