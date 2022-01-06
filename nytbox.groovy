/**
 * https://www.nytimes.com/puzzles/letter-boxed
 */
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.stream.Collectors

def lettersBySide = args[0].replace(' ', '')

def parts = lettersBySide.toList().collate(3, 3)

boolean rotates(String word, List<List<String>> parts, int lastIndex) {
  if (word.length() == 0) {
    return true
  }
  def currIndex = parts.findIndexOf { it.contains(word[0]) }
  if (currIndex == lastIndex) {
    return false
  }
  return rotates(word.substring(1), parts, currIndex)
}

def usableWords = Files.lines(Path.of('/usr/share/dict/words'))
    .map { it.toLowerCase() }
    .filter { it ==~ /^[$lettersBySide]+$/ }
    .filter { word -> rotates(word, parts, -1) }
    .collect(Collectors.toList())

static int scoreWord(String word, List<String> unused) {
  unused.collect { word.contains(it) }.count(true)
}

def choices(List<String> unused, List<String> usableWords, List<String> usedWords, List<List<String>> results, Executor executor) {

  if (!unused) {
    println(usedWords.join(' -> '))
    results.add(usedWords)
    return
  }

  if (usedWords.size() >= 5) {
    return
  }

  if (results.size() > 50) {
    return
  }

  usableWords
      .findAll { it.length() > 3 }
      .findAll { !usedWords.contains(it) }
      .findAll { !usedWords || usedWords[-1][-1] == it[0] }
      .toSorted() { a, b -> scoreWord(b, unused) <=> scoreWord(a, unused) ?: a.length() <=> b.length() }
      .each { newWord ->
        executor.execute { choices(unused.findAll { !newWord.contains(it) }, usableWords, usedWords + [newWord], results, executor) }
      }

}

Executors.newSingleThreadExecutor().with {
  choices(lettersBySide.toList(), usableWords, [], [], it)
}

