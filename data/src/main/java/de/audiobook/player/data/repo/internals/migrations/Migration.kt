package de.audiobook.player.data.repo.internals.migrations

import android.database.sqlite.SQLiteDatabase

/**
 * A database migration
 */
interface Migration {

  fun migrate(db: SQLiteDatabase)
}
