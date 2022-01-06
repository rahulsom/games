/**
 * https://www.nytimes.com/puzzles/spelling-bee
 */
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

def chars = args[0]

assert chars.length() == 7

def words = Files.lines(Path.of('/usr/share/dict/words'))
    .map { it.toLowerCase() }
    .filter { it.contains(chars[0]) }
    .filter { it ==~ /^[$chars]+$/ }
    .filter { it.length() > 3 }
    .collect(Collectors.toList())

def maxLength = words.collect { it.length() }.max()

static def scoreWord(String chars, String word) {
  chars.toList().count { c -> word.any { it == c } }
}

words
    .groupBy { scoreWord(chars, it) }
    .toSorted { a, b -> b.key - a.key }
    .each { k, v ->
      println ""
      println "Chars: $k"
      v.groupBy { it.substring(0, 1) }
          .toSorted { a, b -> a.key <=> b.key }
          .values()
          .each {
            println it.collect { it.padRight(12) }.join(" ")
          }
    }
