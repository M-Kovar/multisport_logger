package com.example.multisport_logger.data.util

import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.ActivityTemplate
import com.example.multisport_logger.data.entity.MonthlyMembership
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ExportData(
    val activityLogs: List<ActivityLog> = emptyList(),
    val templates: List<ActivityTemplate> = emptyList(),
    val monthlyMemberships: List<MonthlyMembership> = emptyList()
)

object DataExportImportUtil {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // JSON Export
    fun exportToJson(
        logs: List<ActivityLog>,
        templates: List<ActivityTemplate>,
        memberships: List<MonthlyMembership>
    ): String {
        val data = ExportData(logs, templates, memberships)
        return json.encodeToString(data)
    }

    // JSON Import
    fun importFromJson(jsonString: String): ExportData {
        return json.decodeFromString<ExportData>(jsonString)
    }

    // CSV Export
    fun exportToCsv(
        logs: List<ActivityLog>,
        templates: List<ActivityTemplate>,
        memberships: List<MonthlyMembership>
    ): String {
        val sb = StringBuilder()

        sb.append("--- LOGS ---\n")
        sb.append("id,date,venueName,activityName,cost\n")
        for (log in logs) {
            sb.append("${log.id},\"${escapeCsv(log.date)}\",\"${escapeCsv(log.venueName)}\",\"${escapeCsv(log.activityName)}\",${log.cost}\n")
        }

        sb.append("\n--- TEMPLATES ---\n")
        sb.append("id,venueName,activityName,defaultCost\n")
        for (tpl in templates) {
            sb.append("${tpl.id},\"${escapeCsv(tpl.venueName)}\",\"${escapeCsv(tpl.activityName)}\",${tpl.defaultCost}\n")
        }

        sb.append("\n--- MEMBERSHIPS ---\n")
        sb.append("yearMonth,cardCost\n")
        for (m in memberships) {
            sb.append("\"${escapeCsv(m.yearMonth)}\",${m.cardCost}\n")
        }

        return sb.toString()
    }

    // CSV Import
    fun importFromCsv(csvString: String): ExportData {
        val logs = mutableListOf<ActivityLog>()
        val templates = mutableListOf<ActivityTemplate>()
        val memberships = mutableListOf<MonthlyMembership>()

        var currentSection = ""

        val lines = csvString.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("--- LOGS ---")) {
                currentSection = "LOGS"
                continue
            } else if (trimmed.startsWith("--- TEMPLATES ---")) {
                currentSection = "TEMPLATES"
                continue
            } else if (trimmed.startsWith("--- MEMBERSHIPS ---")) {
                currentSection = "MEMBERSHIPS"
                continue
            }

            // Header lines check
            if (trimmed.startsWith("id,") || trimmed.startsWith("yearMonth,")) {
                continue
            }

            val tokens = parseCsvLine(trimmed)
            when (currentSection) {
                "LOGS" -> {
                    if (tokens.size >= 5) {
                        logs.add(
                            ActivityLog(
                                id = tokens[0].toLongOrNull() ?: 0,
                                date = tokens[1],
                                venueName = tokens[2],
                                activityName = tokens[3],
                                cost = tokens[4].toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                }
                "TEMPLATES" -> {
                    if (tokens.size >= 4) {
                        templates.add(
                            ActivityTemplate(
                                id = tokens[0].toLongOrNull() ?: 0,
                                venueName = tokens[1],
                                activityName = tokens[2],
                                defaultCost = tokens[3].toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                }
                "MEMBERSHIPS" -> {
                    if (tokens.size >= 2) {
                        memberships.add(
                            MonthlyMembership(
                                yearMonth = tokens[0],
                                cardCost = tokens[1].toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                }
            }
        }

        return ExportData(logs, templates, memberships)
    }

    // Auto-detect JSON or CSV
    fun importData(dataString: String): ExportData {
        val trimmed = dataString.trim()
        return if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            importFromJson(trimmed)
        } else {
            importFromCsv(trimmed)
        }
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && (i + 1 < line.length) && (line[i + 1] == '"')) {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        result.add(sb.toString().trim())
        return result
    }
}
