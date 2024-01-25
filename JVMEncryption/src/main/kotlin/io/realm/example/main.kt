package io.realm.example

import com.jakewharton.fliptables.FlipTableConverters
import io.realm.kotlin.EncryptionKeyCallback
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.annotations.ExperimentalEncryptionCallbackApi
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmObject
import sun.misc.Unsafe

val unsafe: Unsafe by lazy {
    @Suppress("DiscouragedPrivateApi")
    val field = Unsafe::class.java.getDeclaredField("theUnsafe")
    field.isAccessible = true
    field.get(null) as Unsafe
}

fun main(args: Array<String>) {
    // Get AES encryption key from a third party library as a 64 byte array pointer
    val keyPointer = createKeyOnNativeMemory()

    @OptIn(ExperimentalEncryptionCallbackApi::class)
    val realmConfiguration = RealmConfiguration
        .Builder(
            schema = setOf(Author::class)
        )
        .encryptionKey(object : EncryptionKeyCallback {
            override fun keyPointer(): Long = keyPointer
            override fun releaseKey() = freeKeyFromNativeMemory(keyPointer)
        })
        .build()

    val realm = Realm.open(realmConfiguration)

    AuthorsREPL(realm).start()

    realm.close()
}

class Author : RealmObject {
    var firstName: String? = ""
    var lastName: String? = ""
    var age: Int? = 0
}

class AuthorsREPL(private val realm: Realm) {

    fun start() {
        displayAuthors()
        print("Add a new author? (yes/no)\t: ")
        var continueAdding = readLine() ?: "no"

        var firstName: String? = null
        var lastName: String? = null
        var age: Int? = null
        while (continueAdding.equals("yes", ignoreCase = true)) {
            print("First Name\t: ")
            firstName = readLine()
            print("Last Name\t: ")
            lastName = readLine()
            print("Age\t: ")
            age = readLine()?.toInt()

            addAuthor(firstName, lastName, age)
            displayAuthors()

            // continue yes/no
            println("Add a new author? (yes/no)")
            continueAdding = readLine() ?: "no"
        }
    }

    private fun addAuthor(firstName: String?, lastName: String?, age: Int?) {
        realm.writeBlocking {
            copyToRealm(Author().apply {
                this.firstName = firstName
                this.lastName = lastName
                this.age = age
            })
        }
    }

    private fun displayAuthors() {
        val headers = arrayOf("First Name", "Last Name", "Age")
        val authors: RealmResults<Author> = realm.query<Author>().find()
        if (authors.isNotEmpty()) {
           val persistedAuthors =  mutableListOf<Array<String>>().also { data ->
                authors.map { author ->
                    arrayOf(author.firstName ?: "N/A", author.lastName ?: "N/A", author.age?.toString() ?: "N/A").also {
                        data.add(it)
                    }
                }
            }
            println(FlipTableConverters.fromObjects(headers, persistedAuthors.toTypedArray()))
        }
    }
}

// helpers
private fun createKeyOnNativeMemory(): Long {
    // One can use JNI to allocate a byte buffer then write to it.
    // For the sake of simplicity we use sun.misc.Unsafe here to allocate a native buffer
    val keyPointer: Long = unsafe.allocateMemory(64L)
    for (i in 0..63) {
        unsafe.putByte(keyPointer + i, (i%10).toByte())
    }
    return keyPointer
}

private fun freeKeyFromNativeMemory(keyPointer: Long) {
    // zeroing the memory and freeing it
    for (i in 0..63) {
        unsafe.putByte(keyPointer + i, 0)
    }
    unsafe.freeMemory(keyPointer)
}
