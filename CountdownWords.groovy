import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

/**
 * https://www.youtube.com/watch?v=E3vmoac0R2Y
 */

def allowedLetters = args[0].toCharArray().toList()

static def isSubset(List<String> superset, List<String> subset) {
  while(!subset.isEmpty()) {
    def c = subset.pop()
    if (superset.contains(c)) {
      superset.remove(c)
    } else {
      return false
    }
  }
  return true
}

def words = Files.lines(Path.of('/usr/share/dict/words'))
    .map { it.toLowerCase() }
    .collect(Collectors.toList())
    .toSorted { it.length() }
    .findAll { it.length() <= 9 }
    .findAll {
      isSubset(allowedLetters.collect { it.toString() }, it.toCharArray().toList().collect { it.toString() })
    }
    .each {
      println(it)
    }

