package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.R
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.EmergencyContact
import com.example.data.ServiceEvaluation
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.ProtocolGuideline
import com.example.ui.SafetyQuestion
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup local database, repository and ViewModel manually
        val database = AppDatabase.getDatabase(this)
        val repository = AppRepository(
            database.emergencyContactDao(),
            database.serviceEvaluationDao()
        )
        val viewModelFactory = MainViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        setContent {
            var fontScale by remember { mutableStateOf(1.0f) }
            MyApplicationTheme(fontScale = fontScale) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        fontScale = fontScale,
                        onFontScaleChange = { fontScale = it },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.contacts.collectAsState()
    val evaluations by viewModel.evaluations.collectAsState()
    val safetyQuestions by viewModel.safetyQuestions.collectAsState()
    val quizCompleted by viewModel.quizCompleted.collectAsState()
    val hasRisk by viewModel.hasRisk.collectAsState()

    val context = LocalContext.current

    // Help Intent trigger helper
    val dialNumber = { phone: String ->
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível abrir o discador", Toast.LENGTH_SHORT).show()
        }
    }

    val sendSms = { phone: String, message: String ->
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                putExtra("sms_body", message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível enviar mensagem", Toast.LENGTH_SHORT).show()
        }
    }

    val shareApp = {
        try {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Olá! Recomendo muito o aplicativo 'Idoso Seguro', desenvolvido com base nas diretrizes de proteção e acolhimento da Polícia Civil de MS. Acesse por aqui para instalar no seu celular ou no de quem você ama: https://ais-pre-ogwcmumbqw6yopavmog3h5-104493858741.us-east1.run.app")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Aplicativo")
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível abrir o compartilhamento", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_lazy_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. App Header with MS Police/Government context
        item {
            AppHeader()
        }

        // 1.5 Accessibility Font Scaling panel (answering the request to view larger sizes)
        item {
            TextScaleControlCard(
                currentScale = fontScale,
                onScaleChange = onFontScaleChange
            )
        }

        // 2. Safety badge status greeting
        item {
            SafetyWelcomeBadge()
        }

        // 3. Panic & Emergency Center (Large accessible calling buttons)
        item {
            EmergencyPanicCenter(onDial = dialNumber)
        }

        // 4. Custom Emergency Contacts (Room integration)
        item {
            EmergencyContactsSection(
                contacts = contacts,
                onAddContact = { name, relation, phone ->
                    viewModel.addContact(name, relation, phone)
                },
                onDeleteContact = { contact -> viewModel.deleteContact(contact) },
                onCall = dialNumber,
                onSms = { phone ->
                    sendSms(phone, "Olá! Estou usando o aplicativo Idoso Seguro e preciso de ajuda ou de conversar. Por favor, entre em contato comigo!")
                }
            )
        }

        // 5. Safety Questionnaire (Point 7 of Protocol: "DESCOBRIR SITUAÇÕES DE PERIGO")
        item {
            SafetyQuizSection(
                questions = safetyQuestions,
                completed = quizCompleted,
                hasRisk = hasRisk,
                onAnswer = { id, ans -> viewModel.answerQuestion(id, ans) },
                onReset = { viewModel.resetQuiz() }
            )
        }

        // 6. Complete Protocol Guidelines Header & list (Accordions)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Direitos e Protocolos de Proteção",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "O que a Polícia Civil de MS deve garantir a você. Toque em qualquer item para ler detalhes em letras grandes:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }

        items(viewModel.guidelines) { guideline ->
            GuidelineAccordionCard(guideline = guideline)
        }

        // 7. Local Evaluation form (Point 14 of Protocol: "AVALIE O ATENDIMENTO")
        item {
            ServiceEvaluationForm(
                onSubmit = { delegacia, rating, comment ->
                    viewModel.submitEvaluation(delegacia, rating, comment)
                    Toast.makeText(context, "Sua avaliação foi salva localmente com sucesso!", Toast.LENGTH_LONG).show()
                }
            )
        }

        // 8. Log of Saved Service Evaluations
        if (evaluations.isNotEmpty()) {
            item {
                Text(
                    text = "Suas Avaliações Registradas",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(evaluations) { evaluation ->
                EvaluationHistoryCard(
                    evaluation = evaluation,
                    onDelete = { viewModel.deleteEvaluation(evaluation) }
                )
            }
        }

        // 9. QR Code Promotion with CTA to share
        item {
            AppPromotionQrCodeCard(onShare = shareApp)
        }

        // Footer gap
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AppHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Escudo da Polícia",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "POLÍCIA CIVIL DE MS",
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Protocolo de Defesa da Pessoa Idosa",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large illustration banner representing safety
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Casal de idosos felizes e protegidos",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Elegant dark overlay to keep text legible if needed
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )
            }
        }
    }
}

@Composable
fun SafetyWelcomeBadge() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Coração",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Sua vida tem valor!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Este aplicativo foi feito com letras grandes e botões simples para proteger seus direitos e facilitar o contato com a ajuda.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun EmergencyPanicCenter(onDial: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("panic_center_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BOTÕES DE EMERGÊNCIA",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Toque em qualquer botão abaixo para ligar imediatamente:",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Button 1: Polícia Militar 190 (Danger)
            Button(
                onClick = { onDial("190") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .testTag("btn_call_190"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Emergência",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "LIGAR POLÍCIA - 190",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Para perigo ou agressão AGORA",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Button 2: Polícia Civil MS 197 (Inquiry/Report)
            Button(
                onClick = { onDial("197") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .testTag("btn_call_197"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Policy,
                        contentDescription = "Polícia Civil",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "DENUNCIAR / AJUDA - 197",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Falar com a Polícia Civil de MS",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Button 3: Human Rights Disque 100
            Button(
                onClick = { onDial("100") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .testTag("btn_call_100"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Direitos Humanos",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "DIREITOS HUMANOS - 100",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Text(
                            text = "Denunciar maus-tratos ou golpes em segredo",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyContactsSection(
    contacts: List<EmergencyContact>,
    onAddContact: (String, String, String) -> Unit,
    onDeleteContact: (EmergencyContact) -> Unit,
    onCall: (String) -> Unit,
    onSms: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("contacts_section_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meus Contatos de Emergência",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                        .testTag("btn_show_add_contact_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar contato",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = "Cadastre telefones de filhos, parentes ou vizinhos queridos para ligar de forma simples e rápida.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum contato cadastrado ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                contacts.forEach { contact ->
                    ContactListItem(
                        contact = contact,
                        onCall = onCall,
                        onSms = onSms,
                        onDelete = { onDeleteContact(contact) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, relation, phone ->
                onAddContact(name, relation, phone)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ContactListItem(
    contact: EmergencyContact,
    onCall: (String) -> Unit,
    onSms: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contact_item_${contact.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Ícone de contato",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = contact.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${contact.relation}: ${contact.phone}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("btn_delete_contact_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir contato",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Large Calling Button
                Button(
                    onClick = { onCall(contact.phone) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("btn_call_contact_${contact.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Ligar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "LIGAR", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Large SMS Button
                OutlinedButton(
                    onClick = { onSms(contact.phone) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("btn_sms_contact_${contact.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Mandar mensagem",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "AVISAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddContactDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cadastrar Contato",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Escreva as informações com letras grandes abaixo:",
                    style = MaterialTheme.typography.bodyLarge
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Contato", fontSize = 18.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_name"),
                    textStyle = TextStyle(fontSize = 18.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("O que ele é seu? (Ex: Filho, Vizinho)", fontSize = 18.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_relation"),
                    textStyle = TextStyle(fontSize = 18.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone / Celular", fontSize = 18.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_phone"),
                    textStyle = TextStyle(fontSize = 18.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(name, relation, phone)
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_confirm_add_contact")
            ) {
                Text("SALVAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.height(56.dp)
            ) {
                Text("CANCELAR", fontSize = 18.sp)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun SafetyQuizSection(
    questions: List<SafetyQuestion>,
    completed: Boolean,
    hasRisk: Boolean,
    onAnswer: (Int, Boolean) -> Unit,
    onReset: () -> Unit
) {
    val currentQuestion = questions.firstOrNull { it.answer == null }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("safety_quiz_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Text(
                text = "Teste de Segurança Rápido",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Descubra se você está vivendo uma situação de perigo (Baseado no Item 7 do Protocolo Policial).",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (!completed && currentQuestion != null) {
                // Determine question index
                val index = questions.indexOf(currentQuestion) + 1

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pergunta $index de ${questions.size}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = currentQuestion.text,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // SIM button (Danger/Warning color or high contrast)
                        Button(
                            onClick = { onAnswer(currentQuestion.id, true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .testTag("btn_quiz_sim"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("SIM", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // NÃO button (Safe green color)
                        Button(
                            onClick = { onAnswer(currentQuestion.id, false) },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .testTag("btn_quiz_nao"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("NÃO", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            } else {
                // Quiz completed state
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (hasRisk) {
                        // Risky situation detected
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Aviso de Perigo",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "SITUAÇÃO DE RISCO DETECTADA",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Você respondeu 'Sim' para perguntas que indicam risco à sua integridade financeira, física ou mental. \n\nPor favor, procure ajuda imediatamente! Ligue para o 197 (Polícia Civil), 190 (Polícia Militar) ou Disque 100. Seu depoimento na delegacia é em segredo absoluto de justiça.",
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Safe situation
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Tudo Seguro",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "VOCÊ ESTÁ EM SEGURANÇA!",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Que boa notícia! Você não indicou nenhum fator imediato de risco. \n\nContinue sempre acompanhando seus direitos no guia abaixo. Lembre-se: em Mato Grosso do Sul, a Polícia Civil está sempre pronta para lhe acolher com prioridade total se você precisar.",
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onReset,
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("btn_reset_quiz"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refazer")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("REFAZER TESTE DE RISCO", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GuidelineAccordionCard(guideline: ProtocolGuideline) {
    var expanded by remember { mutableStateOf(false) }

    // Map guideline iconName to appropriate vector icons
    val icon = when (guideline.iconName) {
        "menu_book" -> Icons.Default.MenuBook
        "gavel" -> Icons.Default.Shield // Use Shield for law/gavel representation
        "favorite" -> Icons.Default.Favorite
        "meeting_room" -> Icons.Default.Security // Safe meeting
        "sentiment_satisfied" -> Icons.Default.Favorite // Protect feelings
        "accessible" -> Icons.Default.LocalHospital // Health/Access
        "visibility" -> Icons.Default.Warning // Danger tracking
        "lock" -> Icons.Default.Lock // Arrest
        "description" -> Icons.Default.RateReview // Written record
        "local_hospital" -> Icons.Default.LocalHospital // Referrals
        "shield" -> Icons.Default.Shield // Protections
        "star" -> Icons.Default.Star // Gold stars / 80+ priorities
        "vpn_key" -> Icons.Default.Lock // Confidentiality
        "rate_review" -> Icons.Default.RateReview // Review
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { expanded = !expanded }
            .testTag("guideline_card_${guideline.number}"),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (expanded) 2.dp else 1.dp,
            color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle badge with number
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = guideline.number.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (expanded) Color.White else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = guideline.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = guideline.shortDesc,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Fechar" else "Abrir",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Como funciona a regra:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = guideline.detail,
                            fontSize = 19.sp, // Very readable font size
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceEvaluationForm(onSubmit: (String, Int, String) -> Unit) {
    val delegacias = listOf(
        "DPPI - Delegacia Especializada de Campo Grande",
        "1ª Delegacia de Três Lagoas",
        "1ª Delegacia de Dourados",
        "Delegacia de Corumbá",
        "Delegacia de Ponta Porã",
        "Outra Delegacia do Estado de MS"
    )

    var expandedMenu by remember { mutableStateOf(false) }
    var selectedDelegacia by remember { mutableStateOf(delegacias[0]) }
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("evaluation_form_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Avaliar Atendimento Policial",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Se você esteve em uma delegacia em MS, use este formulário simples para dar sua nota (Item 14 do Protocolo). Sua opinião melhora o serviço público!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Delegacia dropdown
            Text(
                text = "Selecione a Delegacia onde foi atendido(a):",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expandedMenu,
                onExpandedChange = { expandedMenu = !expandedMenu },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dropdown_delegacia_box")
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedDelegacia,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("input_delegacia_selected"),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMenu) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    textStyle = TextStyle(fontSize = 18.sp)
                )
                ExposedDropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    delegacias.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item, fontSize = 18.sp, modifier = Modifier.padding(vertical = 4.dp)) },
                            onClick = {
                                selectedDelegacia = item
                                expandedMenu = false
                            },
                            modifier = Modifier.testTag("delegacia_menu_item_${item.hashCode()}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rating Stars
            Text(
                text = "Como foi o atendimento? (Toque nas estrelas):",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Nota $i estrelas",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(56.dp) // Large stars for easy tapping
                            .clickable { rating = i }
                            .padding(4.dp)
                            .testTag("star_rating_$i")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Commentary Text Box
            Text(
                text = "Escreva o seu comentário (Opcional):",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("Ex: Fui atendido de forma muito carinhosa e rápida na sala confortável.", fontSize = 18.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("input_evaluation_comment"),
                textStyle = TextStyle(fontSize = 18.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    onSubmit(selectedDelegacia, rating, comment)
                    comment = ""
                    rating = 5
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("btn_submit_evaluation"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Salvar")
                Spacer(modifier = Modifier.width(10.dp))
                Text("GRAVAR MINHA AVALIAÇÃO", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EvaluationHistoryCard(evaluation: ServiceEvaluation, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("evaluation_history_card_${evaluation.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evaluation.delegacia,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= evaluation.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("btn_delete_evaluation_${evaluation.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover avaliação",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (evaluation.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${evaluation.comment}\"",
                    fontSize = 17.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun TextScaleControlCard(
    currentScale: Float,
    onScaleChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("text_scale_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TAMANHO DA LETRA",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Selecione o melhor tamanho de texto para sua leitura fácil:",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Normal (1.0f)
                val isNormal = currentScale == 1.0f
                Button(
                    onClick = { onScaleChange(1.0f) },
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNormal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isNormal) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (!isNormal) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Text("A\nNormal", textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                // Button 2: Grande (1.25f)
                val isGrande = currentScale == 1.25f
                Button(
                    onClick = { onScaleChange(1.25f) },
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGrande) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isGrande) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (!isGrande) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Text("A+\nGrande", textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // Button 3: Gigante (1.5f)
                val isGigante = currentScale == 1.5f
                Button(
                    onClick = { onScaleChange(1.5f) },
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGigante) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isGigante) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (!isGigante) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Text("A++\nGigante", textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun AppPromotionQrCodeCard(onShare: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("app_promotion_qrcode_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.secondary)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = "Proteção e Divulgação",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DIVULGUE O APLICATIVO",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "Ajude a proteger as pessoas idosas de Mato Grosso do Sul! Compartilhe o aplicativo com seus familiares, vizinhos e amigos idosos.",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Centered QR Code image with a clean frame
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_qrcode_1784159839441),
                    contentDescription = "QR Code para compartilhar o aplicativo Idoso Seguro",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Aponte a câmera do celular para o código acima para abrir e compartilhar, ou clique no botão abaixo para enviar o link de acesso direto pelo WhatsApp ou mensagem:",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = onShare,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("share_app_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Compartilhar",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "COMPARTILHAR APLICATIVO",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
            }
        }
    }
}



