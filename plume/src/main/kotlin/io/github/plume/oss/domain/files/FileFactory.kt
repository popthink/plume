package io.github.plume.oss.domain.files

import net.jpountz.xxhash.StreamingXXHash32
import net.jpountz.xxhash.XXHashFactory
import io.github.plume.oss.Extractor
import java.io.File
import java.io.FileInputStream

/**
 * The factory responsible for obtaining the desired [File] wrapped by its programming language wrapper determined by
 * the file extension.
 */
object FileFactory {
    /**
     * Creates a [File] given the pathname.
     *
     * @param pathname The path at which the file resides.
     * @return A [File] object if not one of the supported file types or a supported file type such as [JavaSourceFile].
     */
    @JvmStatic
    operator fun invoke(pathname: String): PlumeFile {
        return when {
            pathname.endsWith(".java") -> JavaSourceFile(pathname)
            pathname.endsWith(".class") -> JavaClassFile(pathname)
            else -> UnsupportedFile(pathname)
        }
    }

    /**
     * Creates a [File] given the pathname.
     *
     * @param f A generic [File] pointer for the file to cast.
     * @return A [File] object if not one of the supported file types or a supported file type such as [JavaSourceFile].
     */
    @JvmStatic
    operator fun invoke(f: File): PlumeFile {
        return when {
            f.name.endsWith(".java") -> JavaSourceFile(f.absolutePath)
            f.name.endsWith(".class") -> JavaClassFile(f.absolutePath)
            else -> UnsupportedFile(f.absolutePath)
        }
    }

    /**
     * Will ingest a file's contents and return the xxHash32 representation. See
     * [xxHash](https://cyan4973.github.io/xxHash/) for more information.
     *
     * @param f The file to hash.
     * @return The given file's xxHash32 representation
     */
    fun getFileHash(f: File): Int {
        val factory = XXHashFactory.fastestInstance()
        FileInputStream(f).use { inStream ->
            val seed = -0x68b84d74
            val hash32: StreamingXXHash32 = factory.newStreamingHash32(seed)
            val buf = ByteArray(8192)
            while (true) {
                val read = inStream.read(buf)
                if (read == -1) {
                    break
                }
                hash32.update(buf, 0, read)
            }
            return hash32.value
        }
    }
}

/**
 * Class wrapper for Java source files.
 */
class JavaSourceFile internal constructor(pathname: String) : PlumeFile(pathname, PlumeFileType.JAVA_SOURCE)

/**
 * Class wrapper for JVM class files.
 */
class JavaClassFile internal constructor(pathname: String) : PlumeFile(pathname, PlumeFileType.JAVA_CLASS)

/**
 * Class wrapper for unsupported files.
 */
class UnsupportedFile internal constructor (pathname: String) : PlumeFile(pathname, PlumeFileType.UNSUPPORTED)

/**
 * The file types ingested by Plume's [Extractor].
 */
enum class PlumeFileType {
    /**
     * Java is a class-based, object-oriented programming language that is designed to have as few implementation
     * dependencies as possible.
     */
    JAVA_SOURCE,

    /**
     * Java bytecode is the instruction set of the Java virtual machine.
     */
    JAVA_CLASS,

    /**
     * Any file that is not supporte.
     */
    UNSUPPORTED
}