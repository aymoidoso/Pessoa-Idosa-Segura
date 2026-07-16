package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.EmergencyContact
import com.example.data.ServiceEvaluation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Simple structure to represent each of the 14 protocol guidelines
data class ProtocolGuideline(
    val number: Int,
    val title: String,
    val shortDesc: String,
    val detail: String,
    val iconName: String // Visual guide identifier
)

// Safety questionnaire item
data class SafetyQuestion(
    val id: Int,
    val text: String,
    var answer: Boolean? = null // null = unanswered, true = Sim, false = Não
)

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    // Reactive streams from local database
    val contacts: StateFlow<List<EmergencyContact>> = repository.allContacts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val evaluations: StateFlow<List<ServiceEvaluation>> = repository.allEvaluations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Prepopulate a default emergency contact if none exists on startup
    init {
        viewModelScope.launch {
            if (repository.getContactCount() == 0) {
                repository.insertContact(
                    EmergencyContact(
                        name = "Familiar ou Vizinho de Confiança",
                        relation = "Exemplo de Contato",
                        phone = "0800000000"
                    )
                )
            }
        }
    }

    // List of the 14 guidelines of the Civil Police MS Protocol
    val guidelines = listOf(
        ProtocolGuideline(
            number = 1,
            title = "O QUE É ESTE GUIA?",
            shortDesc = "Manual obrigatório de proteção para todo o Mato Grosso do Sul.",
            detail = "É um manual obrigatório criado para a Polícia Civil de Mato Grosso do Sul para orientar o atendimento de pessoas idosas, garantindo proteção e acolhimento em todo o Estado.",
            iconName = "menu_book"
        ),
        ProtocolGuideline(
            number = 2,
            title = "QUEM DEVE APLICAR?",
            shortDesc = "Todas as delegacias e policiais de MS devem seguir este guia.",
            detail = "O protocolo vale para todas as delegacias de Mato Grosso do Sul (especializadas ou gerais). Todos os policiais que atendem o público devem aplicar estas regras.",
            iconName = "gavel"
        ),
        ProtocolGuideline(
            number = 3,
            title = "PRINCÍPIOS DA POLÍCIA",
            shortDesc = "Respeito absoluto, prioridade total e escuta sem pressa.",
            detail = "A polícia garante respeito absoluto à sua dignidade, prioridade total no atendimento, escuta atenta sem julgamentos e paciência. É proibido tratar o idoso como se fosse criança.",
            iconName = "favorite"
        ),
        ProtocolGuideline(
            number = 4,
            title = "O PRIMEIRO ATENDIMENTO",
            shortDesc = "Rápido, em sala reservada, confortável e acolhedor.",
            detail = "Ao chegar à delegacia, você será levado para uma sala confortável, reservada e segura. O atendimento deve ser rápido, urgente e com conversa calma.",
            iconName = "meeting_room"
        ),
        ProtocolGuideline(
            number = 5,
            title = "EVITAR O SEU DESGASTE",
            shortDesc = "Não repetir a história e resolver tudo no primeiro dia.",
            detail = "Você não precisará repetir a mesma história dolorosa para vários policiais. O depoimento é gravado em vídeo para que você não precise retornar desnecessariamente.",
            iconName = "sentiment_satisfied"
        ),
        ProtocolGuideline(
            number = 6,
            title = "ACESSIBILIDADE COMPLETA",
            shortDesc = "Ajuda visual, auditiva, de locomoção e direito a acompanhante.",
            detail = "Garantia de recursos para quem tem dificuldades de audição, visão ou locomoção. Você tem o direito de levar um acompanhante de sua extrema confiança na sala.",
            iconName = "accessible"
        ),
        ProtocolGuideline(
            number = 7,
            title = "DESCOBRIR SITUAÇÕES DE PERIGO",
            shortDesc = "Investigação sobre controle de dinheiro, abandono e cartões presos.",
            detail = "A polícia investiga se você depende do agressor para dinheiro, se sofre abandono, vive isolado ou teve seus documentos ou cartões de aposentadoria presos.",
            iconName = "visibility"
        ),
        ProtocolGuideline(
            number = 8,
            title = "PRISÃO EM FLAGRANTE",
            shortDesc = "Se o crime estiver ocorrendo, a polícia prende o culpado na hora.",
            detail = "Se a agressão, abandono ou golpe estiver ocorrendo naquele momento, a polícia age imediatamente para prender o infrator e afastar você do perigo.",
            iconName = "lock"
        ),
        ProtocolGuideline(
            number = 9,
            title = "O QUE VAI NO DEPOIMENTO?",
            shortDesc = "Histórico de saúde, quem cuida e relatos de violência anterior.",
            detail = "O documento oficial detalha o histórico de agressões anteriores, quem cuida de você no dia a dia, suas condições gerais de saúde e medicamentos necessários.",
            iconName = "description"
        ),
        ProtocolGuideline(
            number = 10,
            title = "PARA ONDE IR DEPOIS?",
            shortDesc = "Encaminhamento para hospitais, CRAS/CREAS e Defensoria.",
            detail = "Após o atendimento policial, você será encaminhado para postos de saúde, assistência social (CRAS, CREAS), Conselho do Idoso ou Defensoria Pública para suporte permanente.",
            iconName = "local_hospital"
        ),
        ProtocolGuideline(
            number = 11,
            title = "MEDIDAS DE PROTEÇÃO",
            shortDesc = "Afastamento do agressor e devolução de cartões e documentos.",
            detail = "O juiz pode decretar o afastamento do agressor, bloqueio de procurações, devolução imediata de seus cartões e documentos, além de encaminhamento para abrigo seguro.",
            iconName = "shield"
        ),
        ProtocolGuideline(
            number = 12,
            title = "CASOS ESPECIAIS (80+)",
            shortDesc = "Maiores de 80 anos têm superprioridade; atendimento em domicílio.",
            detail = "Pessoas com mais de 80 anos passam na frente de todos. Se você estiver muito doente ou acamado e não puder sair, a equipe policial vai até a sua casa para lhe atender.",
            iconName = "star"
        ),
        ProtocolGuideline(
            number = 13,
            title = "SIGILO ABSOLUTO",
            shortDesc = "Tudo o que você disser é protegido por segredo.",
            detail = "O seu depoimento e todos os seus dados pessoais ficam sob segredo absoluto de justiça. Ninguém de fora terá acesso à sua história ou identidade.",
            iconName = "vpn_key"
        ),
        ProtocolGuideline(
            number = 14,
            title = "AVALIE O ATENDIMENTO",
            shortDesc = "Dê sua nota à delegacia para melhorar o serviço público.",
            detail = "Você tem direito de avaliar o atendimento da delegacia dando notas e enviando comentários. Isso ajuda a melhorar o serviço policial em todo o Mato Grosso do Sul.",
            iconName = "rate_review"
        )
    )

    // Safety Questionnaire State
    private val _safetyQuestions = MutableStateFlow(
        listOf(
            SafetyQuestion(1, "Alguém fica com seu dinheiro ou cartão de aposentadoria contra a sua vontade?"),
            SafetyQuestion(2, "Você já sofreu empurrões, xingamentos, ameaças ou agressões de alguém em casa?"),
            SafetyQuestion(3, "Você se sente abandonado, isolado ou sem comida, remédios e higiene básica?"),
            SafetyQuestion(4, "Alguém guardou ou confiscou seus documentos pessoais (RG, CPF, cartões de banco)?"),
            SafetyQuestion(5, "Você sente medo de alguma pessoa que mora com você ou que lhe visita?")
        )
    )
    val safetyQuestions: StateFlow<List<SafetyQuestion>> = _safetyQuestions.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    private val _hasRisk = MutableStateFlow(false)
    val hasRisk: StateFlow<Boolean> = _hasRisk.asStateFlow()

    fun answerQuestion(questionId: Int, answer: Boolean) {
        val updatedList = _safetyQuestions.value.map { q ->
            if (q.id == questionId) q.copy(answer = answer) else q
        }
        _safetyQuestions.value = updatedList

        // Check if all answered
        val allAnswered = updatedList.all { it.answer != null }
        if (allAnswered) {
            val riskDetected = updatedList.any { it.answer == true }
            _hasRisk.value = riskDetected
            _quizCompleted.value = true
        }
    }

    fun resetQuiz() {
        _safetyQuestions.value = _safetyQuestions.value.map { it.copy(answer = null) }
        _quizCompleted.value = false
        _hasRisk.value = false
    }

    // Emergency custom contacts functions
    fun addContact(name: String, relation: String, phone: String) {
        viewModelScope.launch {
            repository.insertContact(
                EmergencyContact(name = name, relation = relation, phone = phone)
            )
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.deleteContactById(contact.id)
        }
    }

    // Evaluation functions
    fun submitEvaluation(delegacia: String, rating: Int, comment: String) {
        viewModelScope.launch {
            repository.insertEvaluation(
                ServiceEvaluation(delegacia = delegacia, rating = rating, comment = comment)
            )
        }
    }

    fun deleteEvaluation(evaluation: ServiceEvaluation) {
        viewModelScope.launch {
            repository.deleteEvaluationById(evaluation.id)
        }
    }
}

// ViewModel factory for injecting repository
class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
