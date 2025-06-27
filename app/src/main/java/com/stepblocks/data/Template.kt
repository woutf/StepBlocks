
package com.stepblocks.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

data class TemplateWithTimeBlocks(
    @Embedded val template: Template,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val timeBlocks: List<TimeBlock>
)
