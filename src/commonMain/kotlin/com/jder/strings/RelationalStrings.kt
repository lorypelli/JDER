package com.jder.strings

object RelationalStrings {
    val TABLE by text("Tabella", "Table")
    val NEW_TABLE by text("NuovaTabella", "NewTable")
    val CREATE_TABLE by text("Crea Tabella", "Create Table")
    val TABLE_ACTIONS by text("Azioni Tabella", "Table Actions")
    val TABLE_PROPERTIES by text("Proprietà Tabella", "Table Properties")
    val TABLE_UPDATED by text("Tabella modificata", "Table updated")
    val TABLE_DELETED by text("Tabella eliminata", "Table deleted")
    val COLUMNS by text("Colonne", "Columns")
    val EDIT_COLUMN by text("Modifica Colonna", "Edit Column")
    val DELETE_COLUMN by text("Elimina colonna", "Delete column")
    val COLUMN_ADDED by text("Colonna aggiunta", "Column added")
    val COLUMN_UPDATED by text("Colonna modificata", "Column updated")
    val COLUMN_DELETED by text("Colonna eliminata", "Column deleted")
    val FOREIGN_KEY by text("Chiave Esterna", "Foreign Key")
    val FOREIGN_KEYS by text("Chiavi esterne", "Foreign keys")
    val NEW_FOREIGN_KEY by text("Nuova Chiave Esterna", "New Foreign Key")
    val CREATE_FOREIGN_KEY by text("Crea Chiave Esterna", "Create Foreign Key")
    val ADD_FOREIGN_KEY by text("Aggiungi Chiave Esterna", "Add Foreign Key")
    val DELETE_FOREIGN_KEY by text("Elimina Chiave Esterna", "Delete Foreign Key")
    val FOREIGN_KEY_ACTIONS by text("Azioni Chiave Esterna", "Foreign Key Actions")
    val FOREIGN_KEY_CREATED by text("Chiave esterna creata", "Foreign key created")
    val FOREIGN_KEY_DELETED by text("Chiave esterna eliminata", "Foreign key deleted")
    val FOREIGN_KEY_TARGET by text("Chiave esterna verso", "Foreign key to")
    val FOREIGN_KEY_REQUIREMENT by
        text(
            "Per creare una chiave esterna serve una colonna chiave primaria o con " +
                "valore unico in una tabella",
            "To create a foreign key you need a primary key column or a column with " +
                "unique values in a table",
        )
    val PRIMARY_KEY_DETAIL by text("Chiave primaria", "Primary key")
    val PRIMARY_KEY_TAG by text("chiave primaria", "primary key")
    val NO_PRIMARY_KEY by text("nessuna", "none")
    val NO_REFERENCE by text("Nessuna", "None")
    val UNIQUE_TAG by text("unica", "unique")
    val OPTIONAL_TAG by text("opzionale", "optional")
    val OPTIONAL by text("Opzionale", "Optional")
    val NULLABLE by text("Opzionale (ammette NULL)", "Optional (allows NULL)")
    val UNIQUE_VALUE by text("Valore Unico", "Unique Value")
    val REFERENCED_TABLE by text("Tabella riferita", "Referenced table")
    val PART_OF_PRIMARY_KEY by text("Parte della chiave primaria", "Part of the primary key")
    val RELATIONAL_EMPTY_HINT by
        text(
            "Scegli Tabella o Nota dalla barra degli strumenti e clicca sulla lavagna.\n" +
                "Per collegare due tabelle, scegli Chiave Esterna e clicca prima sulla tabella " +
                "che riferisce e poi su quella riferita.",
            "Choose Table or Note from the toolbar and click on the canvas.\n" +
                "To connect two tables, choose Foreign Key and click first on the " +
                "referencing table and then on the referenced one.",
        )

    fun references(column: String) = localized("riferisce $column", "references $column")

    fun columnFor(column: String) = localized("Colonna per $column", "Column for $column")

    fun tableWithoutPrimaryKey(table: String) =
        localized(
            "La tabella \"$table\" non ha una chiave primaria: " +
                "aggiungine una prima di creare la chiave esterna",
            "The table \"$table\" has no primary key: add one before creating the foreign key",
        )

    fun columnReferencesMissingTable(column: String, table: String) =
        localized(
            "La colonna $column riferisce una tabella inesistente: $table",
            "The column $column references a nonexistent table: $table",
        )

    fun columnReferencesMissingColumn(column: String, target: String) =
        localized(
            "La colonna $column riferisce una colonna inesistente: $target",
            "The column $column references a nonexistent column: $target",
        )
}
