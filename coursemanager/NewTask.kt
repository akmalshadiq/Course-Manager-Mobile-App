package com.example.coursemanager

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coursemanager.ui.theme.ReminderTheme
import com.google.firebase.firestore.FirebaseFirestore

class NewTask : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReminderTheme {
                NewTaskScreen()
            }
        }
    }
}

@Composable
fun NewTaskScreen() {
    val context = LocalContext.current // Get current context for handling navigation
    val db = FirebaseFirestore.getInstance() // Get Firestore instance

    // States for the form inputs
    var taskName by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf("") }
    var selectedLabel by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFE0F7FA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Simple Back Button
                Button(
                    onClick = {
                        // Navigate back to MainActivity
                        (context as? ComponentActivity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF87CEEB)),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Back",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Task Name Field
                TaskInputField(label = "Task Name", value = taskName) { taskName = it }

                Spacer(modifier = Modifier.height(16.dp))

                // Due Date Field
                TaskInputField(label = "Due Date", value = dueDate) { dueDate = it }

                Spacer(modifier = Modifier.height(24.dp))

                // Course Association Section
                Text(
                    text = "Course Association",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CourseRadioGroup(
                    options = listOf(
                        "TMT3703 Mobile Application Development",
                        "TMF3113 Project Development"
                    ),
                    selectedOption = selectedCourse,
                    onOptionSelected = { selectedCourse = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Label Section
                Text(
                    text = "Label",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CourseRadioGroup(
                    options = listOf("Exam And Quizzes", "Labs"),
                    selectedOption = selectedLabel,
                    onOptionSelected = { selectedLabel = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Add Task Button
                Button(
                    onClick = {
                        // Validate inputs
                        if (taskName.isNotEmpty() && dueDate.isNotEmpty() && selectedCourse.isNotEmpty() && selectedLabel.isNotEmpty()) {
                            val taskData = mapOf(
                                "taskName" to taskName,
                                "dueDate" to dueDate,
                                "courseAssociation" to selectedCourse,
                                "label" to selectedLabel
                            )

                            // Save to Firestore
                            db.collection("Task")
                                .add(taskData)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Task added successfully!", Toast.LENGTH_SHORT).show()
                                    // Clear form inputs
                                    taskName = ""
                                    dueDate = ""
                                    selectedCourse = ""
                                    selectedLabel = ""
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error adding task: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF87CEEB))
                ) {
                    Text(
                        text = "Add Task",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TaskInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(8.dp),
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black)
        )
    }
}

@Composable
fun CourseRadioGroup(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = option == selectedOption,
                    onClick = { onOptionSelected(option) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = option, fontSize = 14.sp, color = Color.Black)
            }
        }
    }
}
