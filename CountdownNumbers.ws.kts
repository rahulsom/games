/*
 * https://www.youtube.com/watch?v=z_GpTDPQDS8
 */
enum class Operator(val str: String) {
  PLUS("+"), MINUS("-"), TIMES("*"), DIVIDE("/")
}

sealed class Maybe<out T> {
  class Just<out T>(val value: T) : Maybe<T>()
  class Nothing<out T> : Maybe<T>()
}

sealed class Term {
  data class Literal(val value: Int) : Term() {
    override fun result() = Maybe.Just(value)
    override fun toString() = "$value"

  }

  data class Expr(val left: Term, val operator: Operator, val right: Term) : Term() {
    override fun result() = when (val lr = left.result()) {
      is Maybe.Nothing -> Maybe.Nothing()
      is Maybe.Just -> when (val rr = right.result()) {
        is Maybe.Nothing -> Maybe.Nothing()
        is Maybe.Just -> {
          when (operator) {
            Operator.PLUS -> Maybe.Just(lr.value + rr.value)
            Operator.MINUS -> Maybe.Just(lr.value - rr.value)
            Operator.TIMES -> Maybe.Just(lr.value * rr.value)
            Operator.DIVIDE ->
              if (rr.value != 0 && lr.value % rr.value == 0)
                Maybe.Just(lr.value / rr.value)
              else
                Maybe.Nothing()
          }
        }
      }
    }

    override fun toString() = "($left ${operator.str} $right)"
  }

  abstract fun result(): Maybe<Int>
}

fun solve(target: Int, numbers: List<Term>, tasks: MutableList<() -> Term?>): Term? {
  if (numbers.size > 1) {
    for (i in numbers.indices) {
      val left = numbers[i]
      for (j in i + 1 until numbers.size) {
        val right = numbers[j]
        listOf(
          Term.Expr(left, Operator.DIVIDE, right),
          Term.Expr(right, Operator.DIVIDE, left),
          Term.Expr(left, Operator.MINUS, right),
          Term.Expr(right, Operator.MINUS, left),
          Term.Expr(left, Operator.PLUS, right),
          Term.Expr(left, Operator.TIMES, right),
        ).forEach { expr ->
          val e = expr.result()
          if (e is Maybe.Just) {
            if (e.value == target) {
              return expr
            } else {
              tasks.add {
                solve(target, listOf(expr) + numbers.filter { it != left && it != right }, tasks)
              }
            }
          }
        }
      }
    }
  }
  return null
}

fun solve(target: Int, numbers: List<Int>) {
  val l = numbers.map { Term.Literal(it) }
  val tasks = mutableListOf<() -> Term?>()
  tasks.add { solve(target, l, tasks) }
  while (tasks.isNotEmpty()) {
    val task = tasks.removeFirst()
    val result = task()
    if (result != null) {
      println(result)
      break
    }
  }
}

if (args.size == 7) {
  val nums = args.map { it.toInt() }
  solve(target = nums.first(), numbers = nums.drop(1))
}
