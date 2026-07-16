package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val contactDao: EmergencyContactDao,
    private val evaluationDao: ServiceEvaluationDao
) {
    val allContacts: Flow<List<EmergencyContact>> = contactDao.getAllContacts()
    val allEvaluations: Flow<List<ServiceEvaluation>> = evaluationDao.getAllEvaluations()

    suspend fun insertContact(contact: EmergencyContact) {
        contactDao.insertContact(contact)
    }

    suspend fun deleteContactById(id: Int) {
        contactDao.deleteContactById(id)
    }

    suspend fun getContactCount(): Int {
        return contactDao.getCount()
    }

    suspend fun insertEvaluation(evaluation: ServiceEvaluation) {
        evaluationDao.insertEvaluation(evaluation)
    }

    suspend fun deleteEvaluationById(id: Int) {
        evaluationDao.deleteEvaluationById(id)
    }
}
