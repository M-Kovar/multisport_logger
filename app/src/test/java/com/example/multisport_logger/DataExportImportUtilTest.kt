package com.example.multisport_logger

import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.ActivityTemplate
import com.example.multisport_logger.data.entity.MonthlyMembership
import com.example.multisport_logger.data.util.DataExportImportUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class DataExportImportUtilTest {

    @Test
    fun testJsonExportAndImport() {
        val logs = listOf(
            ActivityLog(id = 1, date = "2025-05-15", venueName = "Gym Alpha", activityName = "Gym Session", cost = 10.0),
            ActivityLog(id = 2, date = "2025-05-16", venueName = "Pool Beta", activityName = "Swimming", cost = 12.5)
        )
        val templates = listOf(
            ActivityTemplate(id = 1, venueName = "Gym Alpha", activityName = "Gym Session", defaultCost = 10.0)
        )
        val memberships = listOf(
            MonthlyMembership(yearMonth = "2025-05", cardCost = 50.0)
        )

        val json = DataExportImportUtil.exportToJson(logs, templates, memberships)
        val imported = DataExportImportUtil.importFromJson(json)

        assertEquals(2, imported.activityLogs.size)
        assertEquals("Gym Alpha", imported.activityLogs[0].venueName)
        assertEquals(10.0, imported.activityLogs[0].cost, 0.001)

        assertEquals(1, imported.templates.size)
        assertEquals("Gym Session", imported.templates[0].activityName)

        assertEquals(1, imported.monthlyMemberships.size)
        assertEquals("2025-05", imported.monthlyMemberships[0].yearMonth)
        assertEquals(50.0, imported.monthlyMemberships[0].cardCost, 0.001)
    }

    @Test
    fun testCsvExportAndImport() {
        val logs = listOf(
            ActivityLog(id = 1, date = "2025-05-15", venueName = "Gym Alpha", activityName = "Gym Session", cost = 10.0)
        )
        val templates = listOf(
            ActivityTemplate(id = 1, venueName = "Gym Alpha", activityName = "Gym Session", defaultCost = 10.0)
        )
        val memberships = listOf(
            MonthlyMembership(yearMonth = "2025-05", cardCost = 50.0)
        )

        val csv = DataExportImportUtil.exportToCsv(logs, templates, memberships)
        val imported = DataExportImportUtil.importFromCsv(csv)

        assertEquals(1, imported.activityLogs.size)
        assertEquals("Gym Alpha", imported.activityLogs[0].venueName)

        assertEquals(1, imported.templates.size)
        assertEquals(1, imported.monthlyMemberships.size)
    }

    @Test
    fun testAutoDetectImport() {
        val jsonStr = """
            {
                "activityLogs": [
                    {"id": 1, "date": "2025-05-20", "venueName": "Court C", "activityName": "Tennis", "cost": 20.0}
                ],
                "templates": [],
                "monthlyMemberships": []
            }
        """.trimIndent()

        val imported = DataExportImportUtil.importData(jsonStr)
        assertEquals(1, imported.activityLogs.size)
        assertEquals("Tennis", imported.activityLogs[0].activityName)
    }
}
