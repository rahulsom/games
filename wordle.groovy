/**
 * https://www.powerlanguage.co.uk/wordle/
 */
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

List<String> words = Files.lines(Path.of('/usr/share/dict/words'))
    .filter { it.length() == 5 }
    .map { it.toLowerCase() }
    .collect(Collectors.toList())
    .unique()

println """
   Hit return if wordle doesn't recognize a word.
   Hit . for each gray square.
   Hit y for each yellow square.
   Hit g for each green square.
"""

def selectWord(List<String> words) {
  if (words.size() == 1) {
    return words[0]
  }

  def probs = (0..4).collect { idx ->
    words.countBy { it[idx] }
        .toSorted { a, b -> b.value <=> a.value }
  }

  int idx = -1
  def bestProbs = probs
      .collect {
        idx++
        it.collect { [idx, it.key, it.value] }[0]
      }

  def pin = bestProbs
      .findAll { it[2] != words.size() }
      .sort { -it[2] }
      ?.first()

  def newWords = words.findAll { it[pin[0]] == pin[1] }
  return selectWord(newWords)
}

while (true) {
  def testWord = selectWord(words)

  println("# ${testWord}        - There are ${words.size().toString().padLeft(6)} words.")
  if (words.size() == 1) {
    break
  }
  print("> ")
  def feedback = System.in.newReader().readLine()
  if (feedback == '') {
    words = words.findAll { it != testWord }
    continue
  }
  [testWord.toCharArray().toList(), feedback.toCharArray().toList()].transpose()
      .eachWithIndex { kv, idx ->
        def k = kv[0].toString()
        def v = kv[1].toString()

        def used = ''
        if (v == 'y') {
          words = words.findAll { it.contains(k) && it[idx] != k }
          used += k
        }
        if (v == 'g') {
          words = words.findAll { it[idx] == k }
          used += k
        }
        if (v == '.' && !used.contains(k)) {
          words = words.findAll { !it.contains(k) }
        }
      }

}
