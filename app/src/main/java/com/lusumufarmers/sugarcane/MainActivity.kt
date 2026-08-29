package com.lusumufarmers.sugarcane

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Invoice(
    val number: String,
    val farmer: String,
    val company: String,
    val date: String,
    val tonnes: Double,
    val amount: Double,
    val status: String = "ACTIVE"
)

data class CancellationRequest(
    val invoice: String,
    val reason: String,
    val requester: String = "User",
    val status: String = "PENDING"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SugarCaneApp() }
    }
}

@Composable
fun SugarCaneApp() {
    var tab by remember { mutableStateOf(0) }
    var search by remember { mutableStateOf("") }
    var selectedCompany by remember { mutableStateOf("West Kenya") }
    var showRequest by remember { mutableStateOf<Invoice?>(null) }
    var reason by remember { mutableStateOf("") }

    var invoices by remember {
        mutableStateOf(
            listOf(
                Invoice("WK-0001", "John Otieno", "West Kenya", "29-08-2026", 12.5, 68450.0),
                Invoice("WK-0002", "Mary Achieng", "West Kenya", "28-08-2026", 8.0, 43840.0),
                Invoice("MB-0001", "Peter Ouma", "Mumias/Butali", "29-08-2026", 15.0, 82200.0),
                Invoice("MB-0002", "Jane Anyango", "Mumias/Butali", "27-08-2026", 10.0, 54800.0)
            )
        )
    }
    var requests by remember { mutableStateOf(listOf<CancellationRequest>()) }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("SugarCane Invoice Manager") }) },
            bottomBar = {
                NavigationBar {
                    listOf("Dashboard", "Invoices", "Requests", "Admin").forEachIndexed { i, label ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Text(if (i == 0) "⌂" else if (i == 1) "▣" else if (i == 2) "!" else "⚙") },
                            label = { Text(label) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                when (tab) {
                    0 -> Dashboard(invoices, requests)
                    1 -> {
                        Text("Company", style = MaterialTheme.typography.titleMedium)
                        Row(Modifier.fillMaxWidth()) {
                            listOf("West Kenya", "Mumias/Butali").forEach { company ->
                                FilterChip(
                                    selected = selectedCompany == company,
                                    onClick = { selectedCompany = company },
                                    label = { Text(company) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            label = { Text("Search invoice or farmer") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        val filtered = invoices.filter {
                            it.company == selectedCompany &&
                            (search.isBlank() || it.number.contains(search, true) || it.farmer.contains(search, true))
                        }
                        LazyColumn {
                            items(filtered) { inv ->
                                InvoiceCard(inv) {
                                    if (inv.status == "ACTIVE") showRequest = inv
                                }
                            }
                        }
                    }
                    2 -> {
                        Text("Cancellation Requests", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        if (requests.isEmpty()) Text("No cancellation requests.")
                        LazyColumn {
                            items(requests) { req ->
                                Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(req.invoice, style = MaterialTheme.typography.titleMedium)
                                        Text("Reason: ${req.reason}")
                                        Text("Status: ${req.status}")
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        Text("Admin Dashboard", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(12.dp))
                        Text("Active invoices: ${invoices.count { it.status == "ACTIVE" }}")
                        Text("Pending cancellation: ${requests.count { it.status == "PENDING" }}")
                        Text("Cancelled invoices: ${invoices.count { it.status == "CANCELLED" }}")
                        Spacer(Modifier.height(16.dp))
                        Text("Admin actions", style = MaterialTheme.typography.titleLarge)
                        requests.filter { it.status == "PENDING" }.forEach { req ->
                            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Column(Modifier.padding(14.dp)) {
                                    Text("${req.invoice} — ${req.reason}")
                                    Row {
                                        Button(onClick = {
                                            requests = requests.map {
                                                if (it.invoice == req.invoice) it.copy(status = "APPROVED") else it
                                            }
                                            invoices = invoices.map {
                                                if (it.number == req.invoice) it.copy(status = "CANCELLED") else it
                                            }
                                        }) { Text("Approve") }
                                        Spacer(Modifier.width(8.dp))
                                        OutlinedButton(onClick = {
                                            requests = requests.map {
                                                if (it.invoice == req.invoice) it.copy(status = "REJECTED") else it
                                            }
                                        }) { Text("Reject") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showRequest != null) {
            AlertDialog(
                onDismissRequest = { showRequest = null },
                title = { Text("Request invoice cancellation") },
                text = {
                    Column {
                        Text("Invoice: ${showRequest!!.number}")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(enabled = reason.isNotBlank(), onClick = {
                        requests = requests + CancellationRequest(showRequest!!.number, reason)
                        showRequest = null
                        reason = ""
                        tab = 2
                    }) { Text("Submit") }
                },
                dismissButton = {
                    TextButton(onClick = { showRequest = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun Dashboard(invoices: List<Invoice>, requests: List<CancellationRequest>) {
    Text("Dashboard", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(16.dp))
    Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Total invoices", style = MaterialTheme.typography.titleMedium)
            Text("${invoices.size}", style = MaterialTheme.typography.headlineMedium)
        }
    }
    Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Pending requests", style = MaterialTheme.typography.titleMedium)
            Text("${requests.count { it.status == "PENDING" }}", style = MaterialTheme.typography.headlineMedium)
        }
    }
    Text("Companies: West Kenya • Mumias/Butali")
    Spacer(Modifier.height(10.dp))
    Text("Invoices are cancelled through an approval process and remain in the audit history.")
}

@Composable
fun InvoiceCard(inv: Invoice, onRequest: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(inv.number, style = MaterialTheme.typography.titleLarge)
            Text("Farmer: ${inv.farmer}")
            Text("Date: ${inv.date} • ${inv.tonnes} tonnes")
            Text("Amount: KSh ${"%,.2f".format(inv.amount)}")
            Text("Status: ${inv.status}")
            Spacer(Modifier.height(6.dp))
            if (inv.status == "ACTIVE") {
                OutlinedButton(onClick = onRequest) { Text("Request Cancellation") }
            }
        }
    }
}
