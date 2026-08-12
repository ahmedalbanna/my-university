package com.example.data.model

/**
 * Represents a graduation project or academic research project.
 * 
 * @property id Unique identifier for the project.
 * @property title Title of the project.
 * @property studentName Name of the student or team who created the project.
 * @property supervisor Name of the academic supervisor.
 * @property faculty The faculty associated with the project.
 * @property year Graduation year (e.g., "2025").
 * @property level Academic level (usually final year).
 * @property summary A brief overview of the project's goals and results.
 * @property tags List of descriptive keywords (e.g., "AI", "Civil Engineering").
 * @property fileFormat Format of the downloadable file (default is PDF).
 * @property fileSize Size of the project file.
 * @property downloadsCount Number of times the project has been downloaded.
 * @property downloadUrl URL to download the project files.
 */
data class Project(
    val id: String,
    val title: String,
    val studentName: String,
    val supervisor: String,
    val faculty: String,
    val year: String,
    val level: Int,
    val summary: String,
    val tags: List<String>,
    val fileFormat: String = "PDF",
    val fileSize: String = "4.2 MB",
    val downloadsCount: Int = 142,
    val downloadUrl: String = "https://example.com/project.zip"
)
