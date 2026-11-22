package com.example.coursemanager

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coursemanager.ui.theme.ReminderTheme
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.window.Dialog

class Reminder : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReminderTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val db = FirebaseFirestore.getInstance()
    var assignmentTasks by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var miscTasks by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var displayedAssignments by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var displayedMiscTasks by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val searchText = remember { mutableStateOf("") }
    val showFilterDialog = remember { mutableStateOf(false) }

    // Fetch "Assignment" tasks
    LaunchedEffect(Unit) {
        db.collection("Assignment")
            .get()
            .addOnSuccessListener { result ->
                val tasks = result.documents.mapNotNull { it.data }
                assignmentTasks = tasks
                displayedAssignments = tasks
            }
            .addOnFailureListener { e ->
                println("Error fetching assignments: $e")
            }
    }

    // Fetch "Task" tasks
    LaunchedEffect(Unit) {
        db.collection("Task")
            .get()
            .addOnSuccessListener { result ->
                val tasks = result.documents.mapNotNull { it.data }
                miscTasks = tasks
                displayedMiscTasks = tasks
            }
            .addOnFailureListener { e ->
                println("Error fetching tasks: $e")
            }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                HeaderSection()
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(searchText.value) { newSearchText ->
                        searchText.value = newSearchText

                        // Filter assignments
                        displayedAssignments = if (newSearchText.isEmpty()) {
                            assignmentTasks
                        } else {
                            assignmentTasks.filter { task ->
                                task.any { (_, value) ->
                                    value.toString().contains(newSearchText, ignoreCase = true)
                                }
                            }
                        }

                        // Filter miscellaneous tasks
                        displayedMiscTasks = if (newSearchText.isEmpty()) {
                            miscTasks
                        } else {
                            miscTasks.filter { task ->
                                task.any { (_, value) ->
                                    value.toString().contains(newSearchText, ignoreCase = true)
                                }
                            }
                        }
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.filter),
                        contentDescription = "Filter Icon",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                showFilterDialog.value = true
                            }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                TaskSection("Assignments", displayedAssignments, showAddButton = false)
                Spacer(modifier = Modifier.height(16.dp))
                TaskSection("Miscellaneous Tasks", displayedMiscTasks, showAddButton = true)

                if (showFilterDialog.value) {
                    FilterDialog(
                        onDismiss = { showFilterDialog.value = false },
                        onFilterSelected = { filterOption ->
                            showFilterDialog.value = false

                            when (filterOption) {
                                "Assignments" -> {
                                    displayedAssignments = assignmentTasks
                                    displayedMiscTasks = emptyList()
                                }
                                "Miscellaneous Tasks" -> {
                                    displayedAssignments = emptyList()
                                    displayedMiscTasks = miscTasks
                                }
                            }
                        },
                        onClearFilter = {
                            displayedAssignments = assignmentTasks
                            displayedMiscTasks = miscTasks
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterDialog(onDismiss: () -> Unit, onFilterSelected: (String) -> Unit, onClearFilter: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Adjust width
                .wrapContentHeight(), // Allow height to adjust to content
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Filter By",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Divider(color = Color.Gray, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Assignments",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFilterSelected("Assignments") }
                        .padding(8.dp),
                    style = TextStyle(color = Color.Black, fontSize = 16.sp)
                )
                Divider(color = Color.Gray, thickness = 0.5.dp)
                Text(
                    text = "Miscellaneous Tasks",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFilterSelected("Miscellaneous Tasks") }
                        .padding(8.dp),
                    style = TextStyle(color = Color.Black, fontSize = 16.sp)
                )
                Divider(color = Color.Gray, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Clear Filters",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClearFilter() }
                        .padding(8.dp),
                    style = TextStyle(color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun SearchBar(searchText: String, onSearchTextChanged: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = searchText,
            onValueChange = onSearchTextChanged,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            textStyle = TextStyle(fontSize = 16.sp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (searchText.isEmpty()) {
                        Text(
                            text = "Search...",
                            style = TextStyle(color = Color.Gray, fontSize = 16.sp)
                        )
                    }
                    innerTextField()
                }
            }
        )

        Icon(
            painter = painterResource(id = R.drawable.search),
            contentDescription = "Search Icon",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Icon(
            painter = painterResource(id = R.drawable.filter),
            contentDescription = "Filter Icon",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun TaskSection(title: String, tasks: List<Map<String, Any>>, showAddButton: Boolean) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            tasks.forEachIndexed { index, task ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Task ${index + 1}",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    task.forEach { (key, value) ->
                        Text(
                            text = "$key: $value",
                            style = TextStyle(fontSize = 14.sp, color = Color.Black),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
            if (showAddButton) {
                Text(
                    text = "+ Add New Task",
                    style = TextStyle(color = Color(0xFF2C83D4), fontSize = 14.sp),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable {
                            val intent = Intent(context, NewTask::class.java)
                            context.startActivity(intent)
                        }
                )
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFF2C83D4), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "COURSE MANAGER",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ReminderTheme {
        MainScreen()
    }
}
