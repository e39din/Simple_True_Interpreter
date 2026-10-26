# True — system functions

Language version: **0.11.0**

## How to read

- **Description** — one sentence.
- **True** — the sample. The result is in `results[...]`.
- **Inline comment** (only if True is 1 line **and** py/random are 1-line operations):
    - **short** — `\ py: \`...\` | <random>: \`...\`` (same line)
    - **long** — `\ py: \`...\`` on the **next line**
    - **2+ operations** or **multi-line** in py/random — comment omitted.
    - **random** — not py/rust/bash.
    - **no imports** (`import`, `require`, `#include`, `using`, `use`).
- **`<details>`** — all 12 languages (Python, Rust, Bash, Elixir, C, Kotlin, Haskell, Lua, Erlang, JavaScript, Go, Clojure).

**Naming rule:** `= name` is naming. `=>` is never naming. `chain => name` — error if `name` is not declared. `:math`, `:data:*`, `:error`, `:json:*`, `:file:*`, `:api:*` — the result is **already** in `results[...]`; to give a name — `= name` separately, or `:math = name`.

**System names are reserved** (`result`, `parameters`, `nothing`, `step`, `cycle`, `speed`, `color`, `error`, `log`, `message`, `size`, `s`, `math`, `data`, `json`, `file`, `api`, `console`, `simple`, `agent`, `run`, `range`, `repeat`, `activeTab`, `path`, `source`, `test`, `return`, `assert`, `call`, `filter`, `matches`, `map`, `reduce`, `fold`, `each`).

## Table of contents

1. [Math — `:math`](#math)
2. [Data — `:data`](#data)
3. [Functional — `:map` / `:reduce` / `:fold` / `:each`](#func)
4. [JSON — `:json`](#json)
5. [Files — `:file`](#file)
6. [Network — `:api`](#api)
7. [Console — `:console`](#console)
8. [I/O — `:save` / `:load` / `:get` / `:post`](#io)
9. [Errors — `:error`](#error)
10. [Agents — `:agent`](#agent)
11. [Interpreter — `:simple`](#simple)
12. [Settings](#settings)
13. [Meta](#meta)

---

## <a name="math"></a>1. Math — `:math`

### `:math`

**Evaluates a numeric expression (result in `results["math"]`).**

```true
1 + 2 * 3 => :math \ py: `a = 1 + 2 * 3` | elixir: `a = 1 + 2 * 3`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = 1 + 2 * 3` |
| Rust | `let a = 1 + 2 * 3;` |
| Bash | `a=$((1 + 2 * 3))` |
| Elixir | `a = 1 + 2 * 3` |
| C | `int a = 1 + 2 * 3;` |
| Kotlin | `val a = 1 + 2 * 3` |
| Haskell | `let a = 1 + 2 * 3` |
| Lua | `local a = 1 + 2 * 3` |
| Erlang | `A = 1 + 2 * 3.` |
| JavaScript | `const a = 1 + 2 * 3;` |
| Go | `a := 1 + 2*3` |
| Clojure | `(def a (+ 1 (* 2 3)))` |

</details>

---

### `:sum` / `:math:sum`

**Sums all numbers (result in `results["math:sum"]`).**

```true
1, 2, 3, 4, 5 => :sum \ py: `total = sum([1, 2, 3, 4, 5])` | kotlin: `val total = listOf(1, 2, 3, 4, 5).sum()`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `total = sum([1, 2, 3, 4, 5])` |
| Rust | `let total: i32 = [1, 2, 3, 4, 5].iter().sum();` |
| Bash | `total=0; for n in 1 2 3 4 5; do total=$((total+n)); done` |
| Elixir | `total = Enum.sum([1, 2, 3, 4, 5])` |
| C | `int total = 0; int arr[] = {1,2,3,4,5}; for (int i=0;i<5;i++) total += arr[i];` |
| Kotlin | `val total = listOf(1, 2, 3, 4, 5).sum()` |
| Haskell | `let total = sum [1, 2, 3, 4, 5]` |
| Lua | `local total = 1 + 2 + 3 + 4 + 5` |
| Erlang | `Total = lists:sum([1, 2, 3, 4, 5]).` |
| JavaScript | `const total = [1, 2, 3, 4, 5].reduce((x, y) => x + y, 0);` |
| Go | `total := 0; for _, v := range []int{1, 2, 3, 4, 5} { total += v }` |
| Clojure | `(def total (reduce + [1 2 3 4 5]))` |

</details>

---

### `:min` / `:math:min`

**Returns the minimum value (result in `results["math:min"]`).**

```true
3, 7, 2, 9, 4 => :min
\ py: `a = min([3, 7, 2, 9, 4])`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = min([3, 7, 2, 9, 4])` |
| Rust | `let a = [3, 7, 2, 9, 4].iter().min();` |
| Bash | `a=3; for n in 7 2 9 4; do [ "$n" -lt "$a" ] && a=$n; done` |
| Elixir | `a = Enum.min([3, 7, 2, 9, 4])` |
| C | `int a = 3; int arr[] = {3,7,2,9,4}; for (int i=1;i<5;i++) if (arr[i]<a) a=arr[i];` |
| Kotlin | `val a = listOf(3, 7, 2, 9, 4).minOrNull()` |
| Haskell | `let a = minimum [3, 7, 2, 9, 4]` |
| Lua | `local a = math.min(3, 7, 2, 9, 4)` |
| Erlang | `A = lists:min([3, 7, 2, 9, 4]).` |
| JavaScript | `const a = Math.min(...[3, 7, 2, 9, 4]);` |
| Go | `a := 3; for _, v := range []int{7, 2, 9, 4} { if v < a { a = v } }` |
| Clojure | `(def a (apply min [3 7 2 9 4]))` |

</details>

---

### `:max` / `:math:max`

**Returns the maximum value (result in `results["math:max"]`).**

```true
3, 7, 2, 9, 4 => :max
\ py: `a = max([3, 7, 2, 9, 4])`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = max([3, 7, 2, 9, 4])` |
| Rust | `let a = [3, 7, 2, 9, 4].iter().max();` |
| Bash | `a=3; for n in 7 2 9 4; do [ "$n" -gt "$a" ] && a=$n; done` |
| Elixir | `a = Enum.max([3, 7, 2, 9, 4])` |
| C | `int a = 3; int arr[] = {3,7,2,9,4}; for (int i=1;i<5;i++) if (arr[i]>a) a=arr[i];` |
| Kotlin | `val a = listOf(3, 7, 2, 9, 4).maxOrNull()` |
| Haskell | `let a = maximum [3, 7, 2, 9, 4]` |
| Lua | `local a = math.max(3, 7, 2, 9, 4)` |
| Erlang | `A = lists:max([3, 7, 2, 9, 4]).` |
| JavaScript | `const a = Math.max(...[3, 7, 2, 9, 4]);` |
| Go | `a := 3; for _, v := range []int{7, 2, 9, 4} { if v > a { a = v } }` |
| Clojure | `(def a (apply max [3 7 2 9 4]))` |

</details>

---

### `:avg` / `:math:avg`

**Returns the average (result in `results["math:avg"]`).**

```true
1, 2, 3, 4, 5 => :avg
\ py: `a = sum([1, 2, 3, 4, 5]) / len([1, 2, 3, 4, 5])`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = sum([1, 2, 3, 4, 5]) / len([1, 2, 3, 4, 5])` |
| Rust | `let a: f64 = [1, 2, 3, 4, 5].iter().sum::<i32>() as f64 / 5.0;` |
| Bash | `a=$(awk "BEGIN { print (1+2+3+4+5)/5 }")` |
| Elixir | `a = Enum.sum([1,2,3,4,5]) / length([1,2,3,4,5])` |
| C | `int sum=0; int arr[]={1,2,3,4,5}; for(int i=0;i<5;i++) sum+=arr[i]; float a=(float)sum/5;` |
| Kotlin | `val a = listOf(1, 2, 3, 4, 5).average()` |
| Haskell | `let a = sum [1,2,3,4,5] / length [1,2,3,4,5]` |
| Lua | `local a = (1+2+3+4+5) / 5` |
| Erlang | `A = lists:sum([1,2,3,4,5]) / length([1,2,3,4,5]).` |
| JavaScript | `const a = [1,2,3,4,5].reduce((x,y)=>x+y,0) / 5;` |
| Go | `sum := 0; for _, v := range []int{1,2,3,4,5} { sum += v }; a := float64(sum) / 5` |
| Clojure | `(def a (/ (reduce + [1 2 3 4 5]) (count [1 2 3 4 5])))` |

</details>

---

### `:product` / `:math:product`

**Multiplies all numbers (result in `results["math:product"]`).**

```true
1, 2, 3, 4, 5 => :product \ py: `a = math.prod([1, 2, 3, 4, 5])` | haskell: `let a = product [1,2,3,4,5]`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = math.prod([1, 2, 3, 4, 5])` |
| Rust | `let a: i32 = [1, 2, 3, 4, 5].iter().product();` |
| Bash | `a=1; for n in 1 2 3 4 5; do a=$((a*n)); done` |
| Elixir | `a = Enum.reduce([1,2,3,4,5], 1, &(&1*&2))` |
| C | `int a=1; int arr[]={1,2,3,4,5}; for(int i=0;i<5;i++) a*=arr[i];` |
| Kotlin | `val a = listOf(1,2,3,4,5).fold(1) { x, y -> x * y }` |
| Haskell | `let a = product [1,2,3,4,5]` |
| Lua | `local a = 1*2*3*4*5` |
| Erlang | `A = lists:foldl(fun(X, Acc) -> X*Acc end, 1, [1,2,3,4,5]).` |
| JavaScript | `const a = [1,2,3,4,5].reduce((x,y)=>x*y,1);` |
| Go | `a := 1; for _, v := range []int{1,2,3,4,5} { a *= v }` |
| Clojure | `(def a (reduce * [1 2 3 4 5]))` |

</details>

---

### `:count` / `:math:count`

**Counts elements (result in `results["math:count"]`).**

```true
1, 2, 3, 4, 5 => :count \ py: `a = len([1, 2, 3, 4, 5])` | javascript: `const a = [1,2,3,4,5].length;`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = len([1, 2, 3, 4, 5])` |
| Rust | `let a = [1, 2, 3, 4, 5].len();` |
| Bash | `a=5` |
| Elixir | `a = length([1,2,3,4,5])` |
| C | `int a = 5;` |
| Kotlin | `val a = listOf(1,2,3,4,5).size` |
| Haskell | `let a = length [1,2,3,4,5]` |
| Lua | `local a = #{1,2,3,4,5}` |
| Erlang | `A = length([1,2,3,4,5]).` |
| JavaScript | `const a = [1,2,3,4,5].length;` |
| Go | `a := len([]int{1,2,3,4,5})` |
| Clojure | `(def a (count [1 2 3 4 5]))` |

</details>

---

### `:math:sort` (numbers)

**Sorts numbers ascending (result in `results["math:sort"]`).**

```true
3, 1, 2 => :math:sort \ py: `a = sorted([3, 1, 2])` | clojure: `(def a (sort [3 1 2]))`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = sorted([3, 1, 2])` |
| Rust | `let mut a = [3, 1, 2]; a.sort();` |
| Bash | `a=($(printf '%s\n' 3 1 2 | sort -n))` |
| Elixir | `a = Enum.sort([3,1,2])` |
| C | `int a[]={3,1,2}; qsort(a,3,sizeof(int),cmp);` |
| Kotlin | `val a = listOf(3,1,2).sorted()` |
| Haskell | `let a = sort [3,1,2]` |
| Lua | `local a={3,1,2}; table.sort(a)` |
| Erlang | `A = lists:sort([3,1,2]).` |
| JavaScript | `const a = [3,1,2].sort((x,y)=>x-y);` |
| Go | `a := []int{3,1,2}; sort.Ints(a)` |
| Clojure | `(def a (sort [3 1 2]))` |

</details>

---

### `:math:popcount`

**Counts 1 bits (result in `results["math"]`).**

```true
popcount(255) => :math \ py: `a = bin(255).count("1")` | c: `int a = __builtin_popcount(255);`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = bin(255).count("1")` |
| Rust | `let a = 255u32.count_ones();` |
| Bash | `a=$(echo "obase=2;255" \| bc \| tr -cd 1 \| wc -c)` |
| Elixir | `a = 255 \|> Integer.digits(2) \|> Enum.count(&(&1==1))` |
| C | `int a = __builtin_popcount(255);` |
| Kotlin | `val a = 255.countOneBits()` |
| Haskell | `let a = popCount 255` |
| Lua | `local a = 8` |
| Erlang | `A = length([X \|\| <<X:1>> <= <<255>>, X == 1]).` |
| JavaScript | `const a = (255).toString(2).split("1").length - 1;` |
| Go | `a := bits.OnesCount(255)` |
| Clojure | `(def a (Long/bitCount 255))` |

</details>

---

## <a name="data"></a>2. Data — `:data`

### `:data:substring`

**Extracts a substring (result in `results["substring"]`).**

```true
hello => :data:substring \ py: `out = "hello"[0:5]` | rust: `let out = &"hello"[0..5];`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello"[0:5]` |
| Rust | `let out = &"hello"[0..5];` |
| Bash | `out="${s:0:5}"` |
| Elixir | `out = String.slice("hello", 0..4)` |
| C | `char out[6]; strncpy(out, "hello", 5); out[5]='\0';` |
| Kotlin | `val out = "hello".substring(0, 5)` |
| Haskell | `let out = take 5 "hello"` |
| Lua | `local out = string.sub("hello", 1, 5)` |
| Erlang | `Out = string:substr("hello", 1, 5).` |
| JavaScript | `const out = "hello".substring(0, 5);` |
| Go | `out := "hello"[0:5]` |
| Clojure | `(def out (subs "hello" 0 5))` |

</details>

---

### `:data:indexOf`

**Finds a substring position (1-based, result in `results["indexOf"]`).**

```true
hello, ll => :data:indexOf \ py: `out = "hello".find("ll") + 1` | lua: `local out = string.find("hello", "ll")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello".find("ll") + 1` |
| Rust | `let out = "hello".find("ll").map(|i| i + 1);` |
| Bash | `out=$(expr index "hello" ll)` |
| Elixir | `out = :binary.match("hello","ll") \|> elem(0) \|> Kernel.+(1)` |
| C | `char *p = strstr("hello","ll"); int out = p ? (p - "hello" + 1) : -1;` |
| Kotlin | `val out = "hello".indexOf("ll") + 1` |
| Haskell | `let out = 3` |
| Lua | `local out = string.find("hello", "ll")` |
| Erlang | `Out = string:str("hello", "ll").` |
| JavaScript | `const out = "hello".indexOf("ll") + 1;` |
| Go | `out := strings.Index("hello", "ll") + 1` |
| Clojure | `(def out (inc (.indexOf "hello" "ll")))` |

</details>

---

### `:data:replace`

**Replaces all occurrences (result in `results["replace"]`).**

```true
hello, ll, LL => :data:replace \ py: `out = "hello".replace("ll", "LL")` | haskell: `let out = "heLLo"`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello".replace("ll", "LL")` |
| Rust | `let out = "hello".replace("ll", "LL");` |
| Bash | `out="${s//ll/LL}"` |
| Elixir | `out = String.replace("hello","ll","LL")` |
| C | `/* manual */` |
| Kotlin | `val out = "hello".replace("ll", "LL")` |
| Haskell | `let out = "heLLo"` |
| Lua | `local out = string.gsub("hello","ll","LL")` |
| Erlang | `Out = string:replace("hello","ll","LL",all).` |
| JavaScript | `const out = "hello".replace("ll", "LL");` |
| Go | `out := strings.Replace("hello", "ll", "LL", -1)` |
| Clojure | `(def out (clojure.string/replace "hello" "ll" "LL"))` |

</details>

---

### `:data:lower`

**Converts to lowercase (result in `results["lower"]`).**

```true
HELLO => :data:lower \ py: `out = "HELLO".lower()` | lua: `local out = string.lower("HELLO")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "HELLO".lower()` |
| Rust | `let out = "HELLO".to_lowercase();` |
| Bash | `out="${s,,}"` |
| Elixir | `out = String.downcase("HELLO")` |
| C | `char out[6]; for(int i=0;i<5;i++) out[i]=tolower("HELLO"[i]); out[5]=0;` |
| Kotlin | `val out = "HELLO".lowercase()` |
| Haskell | `let out = map toLower "HELLO"` |
| Lua | `local out = string.lower("HELLO")` |
| Erlang | `Out = string:lowercase("HELLO").` |
| JavaScript | `const out = "HELLO".toLowerCase();` |
| Go | `out := strings.ToLower("HELLO")` |
| Clojure | `(def out (clojure.string/lower-case "HELLO"))` |

</details>

---

### `:data:upper`

**Converts to uppercase (result in `results["upper"]`).**

```true
hello => :data:upper \ py: `out = "hello".upper()` | lua: `local out = string.upper("hello")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello".upper()` |
| Rust | `let out = "hello".to_uppercase();` |
| Bash | `out="${s^^}"` |
| Elixir | `out = String.upcase("hello")` |
| C | `char out[6]; for(int i=0;i<5;i++) out[i]=toupper("hello"[i]); out[5]=0;` |
| Kotlin | `val out = "hello".uppercase()` |
| Haskell | `let out = map toUpper "hello"` |
| Lua | `local out = string.upper("hello")` |
| Erlang | `Out = string:uppercase("hello").` |
| JavaScript | `const out = "hello".toUpperCase();` |
| Go | `out := strings.ToUpper("hello")` |
| Clojure | `(def out (clojure.string/upper-case "hello"))` |

</details>

---

### `:data:trim`

**Removes surrounding whitespace (result in `results["trim"]`).**

```true
"  hi  " => :data:trim \ py: `out = "  hi  ".strip()` | kotlin: `val out = "  hi  ".trim()`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "  hi  ".strip()` |
| Rust | `let out = "  hi  ".trim();` |
| Bash | `out=$(echo "  hi  " \| xargs)` |
| Elixir | `out = String.trim("  hi  ")` |
| C | `/* manual trim */` |
| Kotlin | `val out = "  hi  ".trim()` |
| Haskell | `let out = trim "  hi  "` |
| Lua | `local out = "  hi  ":match("^%s*(.-)%s*$")` |
| Erlang | `Out = string:trim("  hi  ").` |
| JavaScript | `const out = "  hi  ".trim();` |
| Go | `out := strings.TrimSpace("  hi  ")` |
| Clojure | `(def out (clojure.string/trim "  hi  "))` |

</details>

---

### `:data:charAt`

**Gets a char by index (1-based, result in `results["charAt"]`).**

```true
hello, 2 => :data:charAt \ py: `out = "hello"[1]` | javascript: `const out = "hello".charAt(1);`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello"[1]` |
| Rust | `let out = "hello".chars().nth(1);` |
| Bash | `out="${s:1:1}"` |
| Elixir | `out = String.at("hello", 1)` |
| C | `char out = "hello"[1];` |
| Kotlin | `val out = "hello"[1]` |
| Haskell | `let out = "hello" !! 1` |
| Lua | `local out = string.sub("hello", 2, 2)` |
| Erlang | `Out = string:substr("hello", 2, 1).` |
| JavaScript | `const out = "hello".charAt(1);` |
| Go | `out := string("hello"[1])` |
| Clojure | `(def out (str (nth "hello" 1)))` |

</details>

---

### `:size` / `:s`

**Returns size by axis (result in `results["size"]`).**

```true
hello => :s \ py: `out = len("hello")` | rust: `let out = "hello".len();`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = len("hello")` |
| Rust | `let out = "hello".len();` |
| Bash | `out=${#s}` |
| Elixir | `out = String.length("hello")` |
| C | `int out = strlen("hello");` |
| Kotlin | `val out = "hello".length` |
| Haskell | `let out = length "hello"` |
| Lua | `local out = #"hello"` |
| Erlang | `Out = length("hello").` |
| JavaScript | `const out = "hello".length;` |
| Go | `out := len("hello")` |
| Clojure | `(def out (count "hello"))` |

</details>

---

### `:data:reverse`

**Reverses a container (result in `results["reverse"]`).**

```true
hello => :data:reverse \ py: `out = "hello"[::-1]` | elixir: `out = String.reverse("hello")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello"[::-1]` |
| Rust | `let out: String = "hello".chars().rev().collect();` |
| Bash | `out=$(echo -n "hello" \| rev)` |
| Elixir | `out = String.reverse("hello")` |
| C | `/* manual */` |
| Kotlin | `val out = "hello".reversed()` |
| Haskell | `let out = reverse "hello"` |
| Lua | `local out = string.reverse("hello")` |
| Erlang | `Out = lists:reverse("hello").` |
| JavaScript | `const out = [..."hello"].reverse().join("");` |
| Go | `r:=[]rune("hello"); for i,j:=0,len(r)-1;i<j;i,j=i+1,j-1{r[i],r[j]=r[j],r[i]}; out:=string(r)` |
| Clojure | `(def out (clojure.string/reverse "hello"))` |

</details>

---

### `:push`

**Appends to a container (result in `results["push"]`).**

```true
1, 2 = arr
arr:y, 3 => :push
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `arr = [1, 2]; arr.append(3)` |
| Rust | `let mut arr = vec![1, 2]; arr.push(3);` |
| Bash | `arr=(1 2); arr+=(3)` |
| Elixir | `arr = [1, 2] ++ [3]` |
| C | `int arr[3]={1,2}; arr[2]=3;` |
| Kotlin | `val arr = mutableListOf(1, 2); arr.add(3)` |
| Haskell | `let arr = [1, 2] ++ [3]` |
| Lua | `local arr = {1, 2}; table.insert(arr, 3)` |
| Erlang | `Arr = [1, 2] ++ [3].` |
| JavaScript | `const arr = [1, 2]; arr.push(3);` |
| Go | `arr := []int{1, 2}; arr = append(arr, 3)` |
| Clojure | `(def arr (conj [1 2] 3))` |

</details>

---

### `:pop`

**Removes and returns the top (result in `results["pop"]`, `results["pop_rest"]`).**

```true
1, 2, 3 = arr
arr:y => :pop
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `arr = [1, 2, 3]; top = arr.pop()` |
| Rust | `let mut arr = vec![1, 2, 3]; let top = arr.pop();` |
| Bash | `arr=(1 2 3); top="${arr[-1]}"; unset 'arr[-1]'` |
| Elixir | `arr = [1, 2, 3]; {top, rest} = List.pop_at(arr, -1)` |
| C | `int arr[3]={1,2,3}; int top=arr[2]; int n=2;` |
| Kotlin | `val arr = mutableListOf(1, 2, 3); val top = arr.removeAt(arr.size - 1)` |
| Haskell | `let arr = [1, 2, 3]; let (top, rest) = (last arr, init arr)` |
| Lua | `local arr = {1, 2, 3}; local top = table.remove(arr)` |
| Erlang | `{Top, Rest} = lists:split(length(Arr) - 1, Arr).` |
| JavaScript | `const arr = [1, 2, 3]; const top = arr.pop();` |
| Go | `arr := []int{1, 2, 3}; top := arr[len(arr)-1]; arr = arr[:len(arr)-1]` |
| Clojure | `(def arr [1 2 3]); (def top (peek arr)); (def rest (pop arr))` |

</details>
## <a name="func"></a>3. Functional — `:map` / `:reduce` / `:fold` / `:each`

### `:map`

**Applies an expression to every element (result in `results["map"]`).**

```true
1, 2, 3 = arr
arr:y, n * 2 => :map
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `arr = [1, 2, 3]; doubles = [n * 2 for n in arr]` |
| Rust | `let doubles: Vec<i32> = arr.into_iter().map(\|n\| n * 2).collect();` |
| Bash | `arr=(1 2 3); doubles=(); for n in "${arr[@]}"; do doubles+=($((n * 2))); done` |
| Elixir | `doubles = Enum.map([1,2,3], &(&1 * 2))` |
| C | `int arr[]={1,2,3}; int doubles[3]; for(int i=0;i<3;i++) doubles[i]=arr[i]*2;` |
| Kotlin | `val doubles = listOf(1,2,3).map { it * 2 }` |
| Haskell | `let doubles = map (*2) [1,2,3]` |
| Lua | `local doubles = {}; for _,v in ipairs({1,2,3}) do table.insert(doubles, v*2) end` |
| Erlang | `Doubles = lists:map(fun(X) -> X*2 end, [1,2,3]).` |
| JavaScript | `const doubles = [1,2,3].map(n => n * 2);` |
| Go | `doubles := []int{}; for _,v := range []int{1,2,3} { doubles = append(doubles, v*2) }` |
| Clojure | `(def doubles (map #(* 2 %) [1 2 3]))` |

</details>

---

### `:reduce`

**Folds a container with an accumulator (result in `results["reduce"]`).**

```true
1, 2, 3 = arr
arr:y, 0, acc + n => :reduce
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `total = reduce(lambda acc, n: acc + n, [1,2,3], 0)` |
| Rust | `let total = [1,2,3].iter().fold(0, \|acc, n\| acc + n);` |
| Bash | `total=0; for n in 1 2 3; do total=$((total+n)); done` |
| Elixir | `total = Enum.reduce([1,2,3], 0, fn n, acc -> acc + n end)` |
| C | `int arr[]={1,2,3}; int total=0; for(int i=0;i<3;i++) total+=arr[i];` |
| Kotlin | `val total = listOf(1,2,3).fold(0) { acc, n -> acc + n }` |
| Haskell | `let total = foldl (+) 0 [1,2,3]` |
| Lua | `local total = 0; for _,v in ipairs({1,2,3}) do total = total + v end` |
| Erlang | `Total = lists:foldl(fun(X, Acc) -> Acc + X end, 0, [1,2,3]).` |
| JavaScript | `const total = [1,2,3].reduce((acc, n) => acc + n, 0);` |
| Go | `total := 0; for _,v := range []int{1,2,3} { total += v }` |
| Clojure | `(def total (reduce + 0 [1 2 3]))` |

</details>

---

### `:fold`

**Synonym for `:reduce` (result in `results["fold"]`).**

```true
1, 2, 3 = arr
arr:y, 1, acc * n => :fold
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `prod = reduce(lambda acc, n: acc * n, [1,2,3], 1)` |
| Rust | `let prod = [1,2,3].iter().fold(1, \|acc, n\| acc * n);` |
| Bash | `prod=1; for n in 1 2 3; do prod=$((prod*n)); done` |
| Elixir | `prod = Enum.reduce([1,2,3], 1, fn n, acc -> acc * n end)` |
| C | `int arr[]={1,2,3}; int prod=1; for(int i=0;i<3;i++) prod*=arr[i];` |
| Kotlin | `val prod = listOf(1,2,3).fold(1) { acc, n -> acc * n }` |
| Haskell | `let prod = foldl (*) 1 [1,2,3]` |
| Lua | `local prod = 1; for _,v in ipairs({1,2,3}) do prod = prod * v end` |
| Erlang | `Prod = lists:foldl(fun(X, Acc) -> Acc * X end, 1, [1,2,3]).` |
| JavaScript | `const prod = [1,2,3].reduce((acc, n) => acc * n, 1);` |
| Go | `prod := 1; for _,v := range []int{1,2,3} { prod *= v }` |
| Clojure | `(def prod (reduce * 1 [1 2 3]))` |

</details>

---

### `:each`

**Executes an operation for every element (side effects only).**

```true
1, 2, 3 = arr
arr:y, n => :log => :each
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `for n in [1,2,3]: print(n)` |
| Rust | `[1,2,3].iter().for_each(\|n\| println!("{}", n));` |
| Bash | `for n in 1 2 3; do echo "$n"; done` |
| Elixir | `Enum.each([1,2,3], fn n -> IO.puts(n) end)` |
| C | `int arr[]={1,2,3}; for(int i=0;i<3;i++) printf("%d\n", arr[i]);` |
| Kotlin | `listOf(1,2,3).forEach { println(it) }` |
| Haskell | `mapM_ print [1,2,3]` |
| Lua | `for _,v in ipairs({1,2,3}) do print(v) end` |
| Erlang | `lists:foreach(fun(X) -> io:format("~p~n", [X]) end, [1,2,3]).` |
| JavaScript | `[1,2,3].forEach(n => console.log(n));` |
| Go | `for _,v := range []int{1,2,3} { fmt.Println(v) }` |
| Clojure | `(doseq [n [1 2 3]] (println n))` |

</details>

---

## <a name="json"></a>4. JSON — `:json`

### `:json:get`

**Gets a value from JSON by key (result in `results["json"]`).**

```true
{'name':'test','value':42} = json
json, name => :json:get
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `result = loads('{"name":"test"}')["name"]` |
| Rust | `let result = data["name"].as_str();` |
| Bash | `result=$(echo '{"name":"test"}' \| jq -r .name)` |
| Elixir | `result = decode!("{\"name\":\"test\"}")["name"]` |
| C | `/* via jansson */` |
| Kotlin | `val result = Json.parseToJsonElement("""{"name":"test"}""").jsonObject["name"]?.toString()` |
| Haskell | `let result = do { obj <- decodeStrict "{\"name\":\"test\"}" :: Maybe Value; withObject "o" (\o -> o .: "name") obj }` |
| Lua | `local result = decode('{"name":"test"}').name` |
| Erlang | `Result = proplists:get_value(<<"name">>, jsx:decode(<<"{\"name\":\"test\"}">>)).` |
| JavaScript | `const result = JSON.parse('{"name":"test"}').name;` |
| Go | `var data map[string]any; json.Unmarshal([]byte(`{"name":"test"}`), &data); result := data["name"]` |
| Clojure | `(def result (:name (parse-string "{\"name\":\"test\"}" true)))` |

</details>

---

### `:json:set`

**Sets a value in JSON by key (result in `results["json"]`).**

```true
{'a':1} = json
json, a, 2 => :json:set
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `d = loads('{"a":1}'); d["a"]=2; result=dumps(d)` |
| Rust | `data["a"] = json!(2); let result = data.to_string();` |
| Bash | `result=$(echo '{"a":1}' \| jq '.a = 2')` |
| Elixir | `result = encode!(Map.put(decode!("{\"a\":1}"), "a", 2))` |
| C | `/* via jansson */` |
| Kotlin | `val obj = Json.parseToJsonElement("""{"a":1}""").jsonObject.toMutableMap(); obj["a"]=JsonPrimitive(2); val result = JsonObject(obj).toString()` |
| Haskell | `-- via aeson + lens` |
| Lua | `local d = decode('{"a":1}'); d.a=2; local result = encode(d)` |
| Erlang | `Result = jsx:encode([{<<"a">>, 2}]).` |
| JavaScript | `const d = JSON.parse('{"a":1}'); d.a=2; const result = JSON.stringify(d);` |
| Go | `data["a"] = 2; result, _ := json.Marshal(data)` |
| Clojure | `(def result (generate-string (assoc (parse-string "{\"a\":1}" true) :a 2)))` |

</details>

---

### `:json:names`

**Gets all top-level keys (result in `results["json"]`).**

```true
{'a':1,'b':2} = json
json => :json:names
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `result = ",".join(loads('{"a":1,"b":2}').keys())` |
| Rust | `let result = data.as_object().unwrap().keys().cloned().collect::<Vec<_>>().join(",");` |
| Bash | `result=$(echo '{"a":1,"b":2}' \| jq -r 'keys_unsorted \| join(",")')` |
| Elixir | `result = Map.keys(decode!("{\"a\":1,\"b\":2}")) \|> Enum.join(",")` |
| C | `/* via jansson */` |
| Kotlin | `val result = Json.parseToJsonElement("""{"a":1,"b":2}""").jsonObject.keys.joinToString(",")` |
| Haskell | `-- via aeson` |
| Lua | `local keys={}; for k in pairs(decode('{"a":1,"b":2}')) do table.insert(keys,k) end; local result=table.concat(keys,",")` |
| Erlang | `Result = string:join([K \|\| {K,_} <- jsx:decode(<<"{\"a\":1,\"b\":2}">>)], ",").` |
| JavaScript | `const result = Object.keys(JSON.parse('{"a":1,"b":2}')).join(",");` |
| Go | `keys := make([]string, 0, len(data)); for k := range data { keys = append(keys, k) }; result := strings.Join(keys, ",")` |
| Clojure | `(def result (clojure.string/join "," (keys (parse-string "{\"a\":1,\"b\":2}" true))))` |

</details>

---

### `:json:values`

**Gets all top-level values (result in `results["json"]`).**

```true
{'a':1,'b':2} = json
json => :json:values
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `result = ",".join(str(v) for v in loads('{"a":1,"b":2}').values())` |
| Rust | `let result = data.as_object().unwrap().values().map(|v| v.to_string()).collect::<Vec<_>>().join(",");` |
| Bash | `result=$(echo '{"a":1,"b":2}' \| jq -r '[.[]] \| join(",")')` |
| Elixir | `result = Map.values(decode!("{\"a\":1,\"b\":2}")) \|> Enum.join(",")` |
| C | `/* via jansson */` |
| Kotlin | `val result = Json.parseToJsonElement("""{"a":1,"b":2}""").jsonObject.values.joinToString(",") { it.toString() }` |
| Haskell | `-- via aeson` |
| Lua | `local values={}; for _,v in pairs(decode('{"a":1,"b":2}')) do table.insert(values,tostring(v)) end; local result=table.concat(values,",")` |
| Erlang | `Result = string:join([integer_to_list(V) \|\| {_,V} <- jsx:decode(<<"{\"a\":1,\"b\":2}">>)], ",").` |
| JavaScript | `const result = Object.values(JSON.parse('{"a":1,"b":2}')).join(",");` |
| Go | `vals := make([]string, 0, len(data)); for _,v := range data { vals = append(vals, fmt.Sprint(v)) }; result := strings.Join(vals, ",")` |
| Clojure | `(def result (clojure.string/join "," (vals (parse-string "{\"a\":1,\"b\":2}" true))))` |

</details>

---

## <a name="file"></a>5. Files — `:file`

### `:file:create`

**Creates a new file with content (result in `results["file"]`).**

```true
/tmp/out.txt, hello => :file:create
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `r = 1 if not exists("/tmp/out.txt") else -1; open("/tmp/out.txt","x").write("hello")` |
| Rust | `OpenOptions::new().write(true).create_new(true).open("/tmp/out.txt")?.write_all(b"hello")?;` |
| Bash | `{ printf 'hello' > /tmp/out.txt; } 2>/dev/null` |
| Elixir | `File.write("/tmp/out.txt", "hello", [:exclusive])` |
| C | `FILE *f=fopen("/tmp/out.txt","wx"); if(f){fputs("hello",f);fclose(f);}` |
| Kotlin | `File("/tmp/out.txt").apply { if (exists()) throw IOException() }.writeText("hello")` |
| Haskell | `withFile "/tmp/out.txt" WriteMode (\h -> hPutStr h "hello")` |
| Lua | `local f = io.open("/tmp/out.txt", "wx"); if f then f:write("hello"); f:close() end` |
| Erlang | `{ok, F} = file:open("/tmp/out.txt", [write, exclusive]), file:write(F, <<"hello">>), file:close(F).` |
| JavaScript | `writeFileSync("/tmp/out.txt", "hello", { flag: "wx" });` |
| Go | `f, _ := os.OpenFile("/tmp/out.txt", os.O_CREATE\|os.O_EXCL\|os.O_WRONLY, 0644); f.WriteString("hello"); f.Close()` |
| Clojure | `(spit "/tmp/out.txt" "hello" :x)` |

</details>

---

### `:file:read`

**Reads a file (result in `results["file"]`).**

```true
/tmp/out.txt => :file:read
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = open("/tmp/out.txt").read()` |
| Rust | `let out = std::fs::read_to_string("/tmp/out.txt")?;` |
| Bash | `out=$(cat /tmp/out.txt)` |
| Elixir | `{:ok, out} = File.read("/tmp/out.txt")` |
| C | `FILE *f=fopen("/tmp/out.txt","r"); char out[1024]; fread(out,1,1024,f); fclose(f);` |
| Kotlin | `val out = File("/tmp/out.txt").readText()` |
| Haskell | `out <- readFile "/tmp/out.txt"` |
| Lua | `local f = io.open("/tmp/out.txt","r"); local out = f:read("*a"); f:close()` |
| Erlang | `{ok, Bin} = file:read_file("/tmp/out.txt"), Out = binary_to_list(Bin).` |
| JavaScript | `const out = readFileSync("/tmp/out.txt", "utf8");` |
| Go | `data, _ := os.ReadFile("/tmp/out.txt"); out := string(data)` |
| Clojure | `(def out (slurp "/tmp/out.txt"))` |

</details>

---

### `:file:update`

**Writes content to a file (result in `results["file"]`).**

```true
/tmp/out.txt, world => :file:update
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `open("/tmp/out.txt","w").write("world")` |
| Rust | `std::fs::write("/tmp/out.txt", "world")?;` |
| Bash | `printf 'world' > /tmp/out.txt` |
| Elixir | `File.write("/tmp/out.txt", "world")` |
| C | `FILE *f=fopen("/tmp/out.txt","w"); fputs("world",f); fclose(f);` |
| Kotlin | `File("/tmp/out.txt").writeText("world")` |
| Haskell | `writeFile "/tmp/out.txt" "world"` |
| Lua | `local f=io.open("/tmp/out.txt","w"); f:write("world"); f:close()` |
| Erlang | `file:write_file("/tmp/out.txt", <<"world">>).` |
| JavaScript | `writeFileSync("/tmp/out.txt", "world");` |
| Go | `os.WriteFile("/tmp/out.txt", []byte("world"), 0644)` |
| Clojure | `(spit "/tmp/out.txt" "world")` |

</details>

---

### `:file:delete`

**Deletes a file (result in `results["file"]`).**

```true
/tmp/out.txt => :file:delete
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `os.remove("/tmp/out.txt")` |
| Rust | `std::fs::remove_file("/tmp/out.txt")?;` |
| Bash | `rm -f /tmp/out.txt` |
| Elixir | `File.rm("/tmp/out.txt")` |
| C | `remove("/tmp/out.txt");` |
| Kotlin | `File("/tmp/out.txt").delete()` |
| Haskell | `removeFile "/tmp/out.txt"` |
| Lua | `os.remove("/tmp/out.txt")` |
| Erlang | `file:delete("/tmp/out.txt").` |
| JavaScript | `unlinkSync("/tmp/out.txt");` |
| Go | `os.Remove("/tmp/out.txt")` |
| Clojure | `(clojure.java.io/delete-file "/tmp/out.txt")` |

</details>

---

### `:file:list`

**Lists files in a directory (result in `results["file"]`).**

```true
/tmp => :file:list
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = ",".join(listdir("/tmp"))` |
| Rust | `let out = std::fs::read_dir("/tmp")?.map(\|e\| e.unwrap().file_name().into_string().unwrap()).collect::<Vec<_>>().join(",");` |
| Bash | `out=$(ls /tmp \| paste -sd, -)` |
| Elixir | `{:ok, files} = File.ls("/tmp"); out = Enum.join(files, ",")` |
| C | `/* via dirent.h */` |
| Kotlin | `val out = File("/tmp").list()?.joinToString(",") ?: ""` |
| Haskell | `files <- listDirectory "/tmp"; let out = intercalate "," files` |
| Lua | `local p = io.popen("ls /tmp"); local out = p:read("*a"); p:close()` |
| Erlang | `{ok, Files} = file:list_dir("/tmp"), Out = string:join(Files, ",").` |
| JavaScript | `const out = readdirSync("/tmp").join(",");` |
| Go | `entries, _ := os.ReadDir("/tmp"); names := []string{}; for _, e := range entries { names = append(names, e.Name()) }; out := strings.Join(names, ",")` |
| Clojure | `(def out (clojure.string/join "," (.list (clojure.java.io/file "/tmp"))))` |

</details>

---

### `:file:exists`

**Checks if a file exists (result in `results["file"]`).**

```true
/tmp/out.txt => :file:exists
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `r = 1 if exists("/tmp/out.txt") else 0` |
| Rust | `let r = if std::path::Path::new("/tmp/out.txt").exists() { 1 } else { 0 };` |
| Bash | `[ -f /tmp/out.txt ] && r=1 \|\| r=0` |
| Elixir | `r = if File.exists?("/tmp/out.txt"), do: 1, else: 0` |
| C | `int r = (access("/tmp/out.txt", F_OK) == 0) ? 1 : 0;` |
| Kotlin | `val r = if (File("/tmp/out.txt").exists()) 1 else 0` |
| Haskell | `e <- doesFileExist "/tmp/out.txt"; let r = if e then 1 else 0` |
| Lua | `local f = io.open("/tmp/out.txt","r"); local r = f and 1 or 0; if f then f:close() end` |
| Erlang | `R = case filelib:is_file("/tmp/out.txt") of true -> 1; false -> 0 end.` |
| JavaScript | `const r = existsSync("/tmp/out.txt") ? 1 : 0;` |
| Go | `r := 0; if _, err := os.Stat("/tmp/out.txt"); err == nil { r = 1 }` |
| Clojure | `(def r (if (.exists (clojure.java.io/file "/tmp/out.txt")) 1 0))` |

</details>

---

### `:file:data`

**Reads or writes a file (symmetric).**

```true
/tmp/out.txt => :file:data
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `data = open("/tmp/out.txt").read()` |
| Rust | `let data = std::fs::read_to_string("/tmp/out.txt")?;` |
| Bash | `data=$(cat /tmp/out.txt)` |
| Elixir | `{:ok, data} = File.read("/tmp/out.txt")` |
| C | `/* via fopen */` |
| Kotlin | `val data = File("/tmp/out.txt").readText()` |
| Haskell | `data' <- readFile "/tmp/out.txt"` |
| Lua | `local f = io.open("/tmp/out.txt","r"); local data = f:read("*a"); f:close()` |
| Erlang | `{ok, Bin} = file:read_file("/tmp/out.txt"), Data = binary_to_list(Bin).` |
| JavaScript | `const data = readFileSync("/tmp/out.txt", "utf8");` |
| Go | `data, _ := os.ReadFile("/tmp/out.txt")` |
| Clojure | `(def data (slurp "/tmp/out.txt"))` |

</details>

---

### `:file:origin`

**Reads or writes the `.orig` version of a file.**

```true
/tmp/out.txt => :file:origin
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `data = open("/tmp/out.txt.orig").read()` |
| Rust | `let data = std::fs::read_to_string("/tmp/out.txt.orig")?;` |
| Bash | `data=$(cat /tmp/out.txt.orig)` |
| Elixir | `{:ok, data} = File.read("/tmp/out.txt.orig")` |
| C | `/* via fopen */` |
| Kotlin | `val data = File("/tmp/out.txt.orig").readText()` |
| Haskell | `data' <- readFile "/tmp/out.txt.orig"` |
| Lua | `local f = io.open("/tmp/out.txt.orig","r"); local data = f:read("*a"); f:close()` |
| Erlang | `{ok, Bin} = file:read_file("/tmp/out.txt.orig").` |
| JavaScript | `const data = readFileSync("/tmp/out.txt.orig", "utf8");` |
| Go | `data, _ := os.ReadFile("/tmp/out.txt.orig")` |
| Clojure | `(def data (slurp "/tmp/out.txt.orig"))` |

</details>

---

### `:file:lines`

**Reads file lines (result in `results["file"]`).**

```true
/tmp/out.txt => :file:lines
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `lines = open("/tmp/out.txt").read().splitlines()` |
| Rust | `let lines: Vec<String> = std::fs::read_to_string("/tmp/out.txt")?.lines().map(String::from).collect();` |
| Bash | `data=$(cat /tmp/out.txt)` |
| Elixir | `{:ok, data} = File.read("/tmp/out.txt"); lines = String.split(data, "\n")` |
| C | `/* via fgets */` |
| Kotlin | `val lines = File("/tmp/out.txt").readLines()` |
| Haskell | `lines' <- lines <$> readFile "/tmp/out.txt"` |
| Lua | `local lines = {}; for l in io.lines("/tmp/out.txt") do table.insert(lines, l) end` |
| Erlang | `{ok, Bin} = file:read_file("/tmp/out.txt"), Lines = string:split(binary_to_list(Bin), "\n", all).` |
| JavaScript | `const lines = readFileSync("/tmp/out.txt", "utf8").split("\n");` |
| Go | `data, _ := os.ReadFile("/tmp/out.txt"); lines := strings.Split(string(data), "\n")` |
| Clojure | `(def lines (clojure.string/split-lines (slurp "/tmp/out.txt")))` |

</details>

---

### `:path`

**Returns the absolute path of a file (result in `results["path"]`).**

```true
:file:main.cycle => :path \ py: `p = abspath("main.cycle")` | elixir: `p = Path.expand("main.cycle")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `p = abspath("main.cycle")` |
| Rust | `let p = std::fs::canonicalize("main.cycle")?;` |
| Bash | `p=$(realpath main.cycle)` |
| Elixir | `p = Path.expand("main.cycle")` |
| C | `char p[PATH_MAX]; realpath("main.cycle", p);` |
| Kotlin | `val p = File("main.cycle").absolutePath` |
| Haskell | `p <- canonicalizePath "main.cycle"` |
| Lua | `local p = io.popen("realpath main.cycle"):read("*a")` |
| Erlang | `{ok, Cwd} = file:get_cwd(), P = filename:join(Cwd, "main.cycle").` |
| JavaScript | `const p = resolve("main.cycle");` |
| Go | `p, _ := filepath.Abs("main.cycle")` |
| Clojure | `(def p (.getAbsolutePath (clojure.java.io/file "main.cycle")))` |

</details>

---

### `file:a => file:b` / `file:a -> file:b` / `file:a => var` / `var => file:b`

**Copy, move, read, write files.**

```true
file:a.txt => file:b.txt
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `copy("a.txt", "b.txt")` |
| Rust | `std::fs::copy("a.txt", "b.txt")?;` |
| Bash | `cp a.txt b.txt` |
| Elixir | `File.cp!("a.txt", "b.txt")` |
| C | `/* manual */` |
| Kotlin | `File("a.txt").copyTo(File("b.txt"))` |
| Haskell | `copyFile "a.txt" "b.txt"` |
| Lua | `os.execute("cp a.txt b.txt")` |
| Erlang | `file:copy("a.txt", "b.txt").` |
| JavaScript | `copyFileSync("a.txt", "b.txt");` |
| Go | `data, _ := os.ReadFile("a.txt"); os.WriteFile("b.txt", data, 0644)` |
| Clojure | `(clojure.java.io/copy (clojure.java.io/file "a.txt") (clojure.java.io/file "b.txt"))` |

</details>

---

## <a name="api"></a>6. Network — `:api`

### `:api:get`

**HTTP GET (result in `results["api:get"]`).**

```true
https://example.com/health => :api:get \ py: `out = get("https://example.com/health").text` | elixir: `{:ok, resp} = HTTPoison.get("https://example.com/health")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = get("https://example.com/health").text` |
| Rust | `let out = reqwest::get("https://example.com/health").await?.text().await?;` |
| Bash | `out=$(curl -s https://example.com/health)` |
| Elixir | `{:ok, resp} = HTTPoison.get("https://example.com/health"); out = resp.body` |
| C | `/* via libcurl */` |
| Kotlin | `val out = java.net.URL("https://example.com/health").readText()` |
| Haskell | `out <- getResponseBody <$> httpLBS "https://example.com/health"` |
| Lua | `local out = require("socket.http").request("https://example.com/health")` |
| Erlang | `{ok,{{_,200,_},_,Body}} = httpc:request("https://example.com/health"), Out = Body.` |
| JavaScript | `const out = await (await fetch("https://example.com/health")).text();` |
| Go | `resp,_ := http.Get("https://example.com/health"); body,_ := io.ReadAll(resp.Body); out := string(body)` |
| Clojure | `(def out (:body (http/get "https://example.com/health")))` |

</details>

---

### `:api:post`

**HTTP POST with body (result in `results["api:post"]`).**

```true
https://example.com/api, {"a":1} => :api:post
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `r = post("https://example.com/api", data='{"a":1}'); out = 1 if r.ok else -1` |
| Rust | `let resp = Client::new().post("https://example.com/api").body(r#"{"a":1}"#).send().await?; let out = if resp.status().is_success() { 1 } else { -1 };` |
| Bash | `code=$(curl -s -o /dev/null -w "%{http_code}" -X POST -d '{"a":1}' https://example.com/api); [ "$code" -lt 300 ] && out=1 \|\| out=-1` |
| Elixir | `{:ok, resp} = HTTPoison.post("https://example.com/api", ~s({"a":1})); out = if resp.status_code < 300, do: 1, else: -1` |
| C | `/* via libcurl */` |
| Kotlin | `val conn = java.net.URL("https://example.com/api").openConnection() as java.net.HttpURLConnection; conn.requestMethod = "POST"; conn.doOutput = true; conn.outputStream.write("""{"a":1}""".toByteArray()); val out = if (conn.responseCode < 300) 1 else -1` |
| Haskell | `req <- parseRequest "POST https://example.com/api"; resp <- httpLBS =<< setRequestBodyString "{\"a\":1}" <$> pure req; let out = if getResponseStatusCode resp < 300 then 1 else -1` |
| Lua | `-- via luasocket` |
| Erlang | `{ok,{{_,Code,_},_,_}} = httpc:request(post, {"https://example.com/api", [], "application/json", <<"{\"a\":1}">>}, [], []), Out = case Code < 300 of true -> 1; false -> -1 end.` |
| JavaScript | `fetch("https://example.com/api", { method: "POST", body: '{"a":1}' }).then(r => console.log(r.ok ? 1 : -1));` |
| Go | `resp, _ := http.Post("https://example.com/api", "application/json", strings.NewReader(`{"a":1}`)); out := -1; if resp.StatusCode < 300 { out = 1 }` |
| Clojure | `(def resp (http/post "https://example.com/api" {:body "{\"a\":1}"})); (def out (if (< (:status resp) 300) 1 -1))` |

</details>

---

### `:api:put` / `:api:delete` / `:api:patch` / `:api:websocket`

**Other HTTP methods and WebSocket (result in `results["api:<method>"]`).**

```true
https://example.com/api, {"a":2} => :api:put
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `put("https://example.com/api", data='{"a":2}')` |
| Rust | `Client::new().put("https://example.com/api").body(r#"{"a":2}"#).send().await?;` |
| Bash | `curl -X PUT -d '{"a":2}' https://example.com/api` |
| Elixir | `HTTPoison.put("https://example.com/api", ~s({"a":2}))` |
| C | `/* via libcurl */` |
| Kotlin | `// same pattern as :api:post` |
| Haskell | `-- same pattern` |
| Lua | `-- via luasocket` |
| Erlang | `httpc:request(put, {"https://example.com/api", [], "application/json", <<"{\"a\":2}">>}, [], []).` |
| JavaScript | `fetch("https://example.com/api", { method: "PUT", body: '{"a":2}' });` |
| Go | `req, _ := http.NewRequest("PUT", "https://example.com/api", strings.NewReader(`{"a":2}`)); http.DefaultClient.Do(req)` |
| Clojure | `(http/put "https://example.com/api" {:body "{\"a\":2}"})` |

</details>

## <a name="console"></a>7. Console — `:console`

### `:console`

**Executes an OS command (result in `results["console"]`).**

```true
ls -la => :console
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = check_output("ls -la", shell=True).decode()` |
| Rust | `let out = Command::new("sh").arg("-c").arg("ls -la").output()?.stdout;` |
| Bash | `out=$(ls -la)` |
| Elixir | `{out, _} = System.cmd("sh", ["-c", "ls -la"])` |
| C | `FILE *p = popen("ls -la", "r"); /* read p */ pclose(p);` |
| Kotlin | `val out = Runtime.getRuntime().exec("ls -la").inputStream.bufferedReader().readText()` |
| Haskell | `out <- readProcess "ls" ["-la"] ""` |
| Lua | `local p = io.popen("ls -la"); local out = p:read("*a"); p:close()` |
| Erlang | `Out = os:cmd("ls -la").` |
| JavaScript | `const out = execSync("ls -la").toString();` |
| Go | `outBytes, _ := exec.Command("sh", "-c", "ls -la").Output(); out := string(outBytes)` |
| Clojure | `(def out (:out (clojure.java.shell/sh "ls" "-la")))` |

</details>

---

## <a name="io"></a>8. I/O — `:save` / `:load` / `:get` / `:post`

### `:save`

**Saves data via HTTP POST (result in `results["save"]`).**

```true
/tmp/out.txt, hello => :save
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `r = post("/tmp/out.txt", data="hello"); out = 1 if r.ok else -1` |
| Rust | `let resp = Client::new().post("/tmp/out.txt").body("hello").send().await?; let out = if resp.status().is_success() { 1 } else { -1 };` |
| Bash | `curl -s -o /dev/null -X POST -d 'hello' /tmp/out.txt && out=1 \|\| out=-1` |
| Elixir | `{:ok, resp} = HTTPoison.post("/tmp/out.txt", "hello"); out = if resp.status_code < 300, do: 1, else: -1` |
| C | `/* via libcurl */` |
| Kotlin | `val conn = java.net.URL("/tmp/out.txt").openConnection() as java.net.HttpURLConnection; conn.requestMethod = "POST"; conn.doOutput = true; conn.outputStream.write("hello".toByteArray()); val out = if (conn.responseCode < 300) 1 else -1` |
| Haskell | `resp <- httpLBS =<< setRequestBodyString "hello" <$> parseRequest "POST /tmp/out.txt"; let out = if getResponseStatusCode resp < 300 then 1 else -1` |
| Lua | `-- via luasocket` |
| Erlang | `httpc:request(post, {"/tmp/out.txt", [], "", "hello"}, [], []).` |
| JavaScript | `fetch("/tmp/out.txt", { method: "POST", body: "hello" }).then(r => console.log(r.ok ? 1 : -1));` |
| Go | `resp, _ := http.Post("/tmp/out.txt", "text/plain", strings.NewReader("hello")); out := -1; if resp.StatusCode < 300 { out = 1 }` |
| Clojure | `(http/post "/tmp/out.txt" {:body "hello"})` |

</details>

---

### `:load`

**Loads data via HTTP GET (result in `results["load"]`).**

```true
/tmp/in.txt => :load
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = get("/tmp/in.txt").text` |
| Rust | `let out = reqwest::get("/tmp/in.txt").await?.text().await?;` |
| Bash | `out=$(curl -s /tmp/in.txt)` |
| Elixir | `{:ok, resp} = HTTPoison.get("/tmp/in.txt"); out = resp.body` |
| C | `/* via libcurl */` |
| Kotlin | `val out = java.net.URL("/tmp/in.txt").readText()` |
| Haskell | `out <- getResponseBody <$> httpLBS "/tmp/in.txt"` |
| Lua | `local out = require("socket.http").request("/tmp/in.txt")` |
| Erlang | `{ok,{{_,200,_},_,Body}} = httpc:request("/tmp/in.txt"), Out = Body.` |
| JavaScript | `fetch("/tmp/in.txt").then(r => r.text()).then(console.log);` |
| Go | `resp, _ := http.Get("/tmp/in.txt"); body, _ := io.ReadAll(resp.Body); out := string(body)` |
| Clojure | `(def out (:body (http/get "/tmp/in.txt")))` |

</details>

---

### `:get`

**HTTP GET (result in `results["get"]`).**

```true
/tmp/in.txt => :get
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = get("/tmp/in.txt").text` |
| Rust | `let out = reqwest::get("/tmp/in.txt").await?.text().await?;` |
| Bash | `out=$(curl -s /tmp/in.txt)` |
| Elixir | `{:ok, resp} = HTTPoison.get("/tmp/in.txt"); out = resp.body` |
| C | `/* via libcurl */` |
| Kotlin | `val out = java.net.URL("/tmp/in.txt").readText()` |
| Haskell | `out <- getResponseBody <$> httpLBS "/tmp/in.txt"` |
| Lua | `local out = require("socket.http").request("/tmp/in.txt")` |
| Erlang | `{ok,{{_,200,_},_,Body}} = httpc:request("/tmp/in.txt"), Out = Body.` |
| JavaScript | `const out = await (await fetch("/tmp/in.txt")).text();` |
| Go | `resp, _ := http.Get("/tmp/in.txt"); body, _ := io.ReadAll(resp.Body); out := string(body)` |
| Clojure | `(def out (:body (http/get "/tmp/in.txt")))` |

</details>

---

### `:post`

**HTTP POST with body (result in `results["post"]`).**

```true
/tmp/out.txt, hello => :post
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `r = post("/tmp/out.txt", data="hello"); out = 1 if r.ok else -1` |
| Rust | `let resp = Client::new().post("/tmp/out.txt").body("hello").send().await?; let out = if resp.status().is_success() { 1 } else { -1 };` |
| Bash | `curl -s -o /dev/null -X POST -d 'hello' /tmp/out.txt && out=1 \|\| out=-1` |
| Elixir | `{:ok, resp} = HTTPoison.post("/tmp/out.txt", "hello"); out = if resp.status_code < 300, do: 1, else: -1` |
| C | `/* via libcurl */` |
| Kotlin | `val conn = java.net.URL("/tmp/out.txt").openConnection() as java.net.HttpURLConnection; conn.requestMethod = "POST"; conn.doOutput = true; conn.outputStream.write("hello".toByteArray()); val out = if (conn.responseCode < 300) 1 else -1` |
| Haskell | `resp <- httpLBS =<< setRequestBodyString "hello" <$> parseRequest "POST /tmp/out.txt"; let out = if getResponseStatusCode resp < 300 then 1 else -1` |
| Lua | `-- via luasocket` |
| Erlang | `httpc:request(post, {"/tmp/out.txt", [], "", "hello"}, [], []).` |
| JavaScript | `fetch("/tmp/out.txt", { method: "POST", body: "hello" }).then(r => console.log(r.ok ? 1 : -1));` |
| Go | `resp, _ := http.Post("/tmp/out.txt", "text/plain", strings.NewReader("hello")); out := -1; if resp.StatusCode < 300 { out = 1 }` |
| Clojure | `(http/post "/tmp/out.txt" {:body "hello"})` |

</details>

---

## <a name="error"></a>9. Errors — `:error`

### `:error`

**Returns `"1"` if there are errors, `"0"` otherwise (result in `results["error"]`).**

```true
:error
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `dst = "1" if error_flag else "0"` |
| Rust | `let dst = if error_flag { "1" } else { "0" };` |
| Bash | `dst=$([ "$error_flag" = "1" ] && echo 1 \|\| echo 0)` |
| Elixir | `dst = if error_flag, do: "1", else: "0"` |
| C | `const char *dst = error_flag ? "1" : "0";` |
| Kotlin | `val dst = if (errorFlag) "1" else "0"` |
| Haskell | `let dst = if errorFlag then "1" else "0"` |
| Lua | `local dst = error_flag and "1" or "0"` |
| Erlang | `Dst = case ErrorFlag of true -> "1"; false -> "0" end.` |
| JavaScript | `const dst = errorFlag ? "1" : "0";` |
| Go | `dst := "0"; if errorFlag { dst = "1" }` |
| Clojure | `(def dst (if error-flag "1" "0"))` |

</details>

---

### `:error:line`

**Returns the last error's line number (result in `results["error:line"]`).**

```true
:error:line
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `l = str(exception_line)` |
| Rust | `let l = error_line.to_string();` |
| Bash | `l=$error_line` |
| Elixir | `l = Integer.to_string(error_line)` |
| C | `char l[16]; sprintf(l, "%d", error_line);` |
| Kotlin | `val l = errorLine.toString()` |
| Haskell | `let l = show errorLine` |
| Lua | `local l = tostring(error_line)` |
| Erlang | `L = integer_to_list(ErrorLine).` |
| JavaScript | `const l = String(errorLine);` |
| Go | `l := strconv.Itoa(errorLine)` |
| Clojure | `(def l (str error-line))` |

</details>

---

### `:error:message`

**Returns the last error message (result in `results["error:message"]`).**

```true
:error:message
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `m = f"{code}, {1 if active else 0}, {details}"` |
| Rust | `let m = format!("{}, {}, {}", code, if active { 1 } else { 0 }, details);` |
| Bash | `m="$code, $([ "$active" = "1" ] && echo 1 \|\| echo 0), $details"` |
| Elixir | `m = "#{code}, #{if active, do: 1, else: 0}, #{details}"` |
| C | `char m[1024]; sprintf(m, "%s, %d, %s", code, active ? 1 : 0, details);` |
| Kotlin | `val m = "$code, ${if (active) 1 else 0}, $details"` |
| Haskell | `let m = code ++ ", " ++ (if active then "1" else "0") ++ ", " ++ details` |
| Lua | `local m = code .. ", " .. (active and "1" or "0") .. ", " .. details` |
| Erlang | `M = io_lib:format("~s, ~p, ~s", [Code, case Active of true -> 1; false -> 0 end, Details]).` |
| JavaScript | `const m = `${code}, ${active ? 1 : 0}, ${details}`;` |
| Go | `m := fmt.Sprintf("%s, %d, %s", code, map[bool]int{true: 1, false: 0}[active], details)` |
| Clojure | `(def m (str code ", " (if active 1 0) ", " details))` |

</details>

---

### `:error:lines` / `:error:messages`

**All error lines / all error messages.**

```true
:error:lines
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `ls = ",".join(str(l) for l in error_lines)` |
| Rust | `let ls = error_lines.iter().map(\|l\| l.to_string()).collect::<Vec<_>>().join(",");` |
| Bash | `ls=$(IFS=,; echo "${error_lines[*]}")` |
| Elixir | `ls = Enum.join(error_lines, ",")` |
| C | `/* manual assembly */` |
| Kotlin | `val ls = errorLines.joinToString(",")` |
| Haskell | `let ls = intercalate "," (map show errorLines)` |
| Lua | `local ls = table.concat(error_lines, ",")` |
| Erlang | `Ls = string:join([integer_to_list(L) \|\| L <- ErrorLines], ",").` |
| JavaScript | `const ls = errorLines.join(",");` |
| Go | `ls := strings.Join(toStrings(errorLines), ",")` |
| Clojure | `(def ls (clojure.string/join "," error-lines))` |

</details>

---

### `message => :error`

**Sets an error with the given message (assert-like).**

```true
!(a == b) ? "a != b" => :error
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `if not (a == b): raise AssertionError("a != b")` |
| Rust | `assert_eq!(a, b, "a != b");` |
| Bash | `[ "$a" = "$b" ] \|\| { echo "a != b" >&2; exit 1; }` |
| Elixir | `if a != b, do: raise "a != b"` |
| C | `if (a != b) { fprintf(stderr, "a != b\n"); exit(1); }` |
| Kotlin | `if (a != b) throw AssertionError("a != b")` |
| Haskell | `if a /= b then error "a != b" else ()` |
| Lua | `if a ~= b then error("a != b") end` |
| Erlang | `case A == B of true -> ok; false -> erlang:error("a != b") end.` |
| JavaScript | `if (a !== b) throw new Error("a != b");` |
| Go | `if a != b { panic("a != b") }` |
| Clojure | `(when (not= a b) (throw (AssertionError. "a != b")))` |

</details>

---

### `0 => :error:resolve`

**Marks all errors as resolved (green in editor and log).**

```true
0 => :error:resolve
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `for e in errors: e.resolved = True` |
| Rust | `for e in errors.iter_mut() { e.resolved = true; }` |
| Bash | `# not idiomatic` |
| Elixir | `Enum.each(errors, fn e -> Map.put(e, :resolved, true) end)` |
| C | `for (int i = 0; i < n; i++) errors[i].resolved = 1;` |
| Kotlin | `errors.forEach { it.resolved = true }` |
| Haskell | `let errors' = map (\e -> e { resolved = True }) errors` |
| Lua | `for _, e in ipairs(errors) do e.resolved = true end` |
| Erlang | `Errors2 = [E#err{resolved = true} \|\| E <- Errors].` |
| JavaScript | `errors.forEach(e => e.resolved = true);` |
| Go | `for i := range errors { errors[i].Resolved = true }` |
| Clojure | `(doseq [e errors] (swap! e assoc :resolved true))` |

</details>

## <a name="agent"></a>10. Agents — `:agent`

### `:agent:<name>:status`

**Gets a named agent's status (result in `results[dst]`).**

```true
:agent:main:status = dst \ py: `dst = agents["main"].status` | elixir: `dst = Agent.get(:"main", & &1.status)`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `dst = agents["main"].status` |
| Rust | `let dst = main_handle.state().to_string();` |
| Bash | `dst=$main_status` |
| Elixir | `dst = Agent.get(:"main", & &1.status)` |
| C | `const char *dst = main_agent.status_name;` |
| Kotlin | `val dst = agentStatus["main"]` |
| Haskell | `dst <- readMVar mainStatus` |
| Lua | `local dst = coroutine.status(main)` |
| Erlang | `Dst = sys:get_state(MainPid).` |
| JavaScript | `const dst = agents["main"].status;` |
| Go | `dst := agents["main"].Status` |
| Clojure | `(def dst (:status @main-agent))` |

</details>

---

### `:agent:<name>:waiter`

**Checks if the agent is a waiter (result in `results[dst]`).**

```true
:agent:main:waiter = dst \ py: `dst = "1" if main_waiter else "0"` | elixir: `dst = if main_waiter, do: "1", else: "0"`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `dst = "1" if main_waiter else "0"` |
| Rust | `let dst = if main_waiter { "1" } else { "0" };` |
| Bash | `dst=$([ "$main_waiter" = "1" ] && echo 1 \|\| echo 0)` |
| Elixir | `dst = if main_waiter, do: "1", else: "0"` |
| C | `const char *dst = main_waiter ? "1" : "0";` |
| Kotlin | `val dst = if (mainWaiter) "1" else "0"` |
| Haskell | `let dst = if mainWaiter then "1" else "0"` |
| Lua | `local dst = main_waiter and "1" or "0"` |
| Erlang | `Dst = case MainWaiter of true -> "1"; false -> "0" end.` |
| JavaScript | `const dst = mainWaiter ? "1" : "0";` |
| Go | `dst := "0"; if mainWaiter { dst = "1" }` |
| Clojure | `(def dst (if @main-waiter "1" "0"))` |

</details>

---

### `:agent:status`

**Gets all agents' statuses (result in `results[dst]`).**

```true
:agent:status = dst
\ py: `dst = ",".join(f"{k}:{v.status}" for k, v in agents.items())`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `dst = ",".join(f"{k}:{v.status}" for k, v in agents.items())` |
| Rust | `let dst = agents.iter().map(\|(k, v)\| format!("{}:{}", k, v.status)).collect::<Vec<_>>().join(",");` |
| Bash | `dst=$(for k in "${!agents[@]}"; do echo -n "$k:${agents[$k]},"; done \| sed 's/,$//')` |
| Elixir | `dst = Enum.map_join(agents, ",", fn {k, v} -> "#{k}:#{v.status}" end)` |
| C | `/* manual assembly */` |
| Kotlin | `val dst = agents.entries.joinToString(",") { "${it.key}:${it.value.status}" }` |
| Haskell | `let dst = intercalate "," (map (\(k, v) -> k ++ ":" ++ show (agentStatus v)) (Map.toList agents))` |
| Lua | `local parts = {}; for k, v in pairs(agents) do table.insert(parts, k .. ":" .. v.status) end; local dst = table.concat(parts, ",")` |
| Erlang | `Dst = string:join([atom_to_list(K) ++ ":" ++ atom_to_list(V#agent.status) \|\| {K, V} <- Agents], ",").` |
| JavaScript | `const dst = Object.entries(agents).map(([k, v]) => `${k}:${v.status}`).join(",");` |
| Go | `parts := []string{}; for k, v := range agents { parts = append(parts, k+":"+v.Status) }; dst := strings.Join(parts, ",")` |
| Clojure | `(def dst (clojure.string/join "," (map (fn [[k v]] (str k ":" (:status v))) @agents)))` |

</details>

---

### `:agent:wait`

**Pauses the agent for N ms (result: `PAUSED`, resumes after N ms).**

```true
100 => :agent:wait
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `time.sleep(0.1)` |
| Rust | `std::thread::sleep(std::time::Duration::from_millis(100));` |
| Bash | `sleep 0.1` |
| Elixir | `Process.sleep(100)` |
| C | `usleep(100 * 1000);` |
| Kotlin | `Thread.sleep(100)` |
| Haskell | `threadDelay 100000` |
| Lua | `-- via socket.sleep` |
| Erlang | `timer:sleep(100).` |
| JavaScript | `await new Promise(r => setTimeout(r, 100));` |
| Go | `time.Sleep(100 * time.Millisecond)` |
| Clojure | `(Thread/sleep 100)` |

</details>

---

### `:agent:wait:timeout`

**Pauses the agent for N ms, but no longer than T ms (result: `PAUSED`, auto-resume with `timeout` error if exceeded).**

```true
100, 5000 => :agent:wait:timeout
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `time.sleep(min(0.1, 5.0))` |
| Rust | `tokio::time::timeout(Duration::from_millis(5000), tokio::time::sleep(Duration::from_millis(100))).await;` |
| Bash | `sleep 0.1 & wait -n` |
| Elixir | `Process.sleep(min(100, 5000))` |
| C | `/* via select/poll */` |
| Kotlin | `withTimeout(5000) { delay(100) }` |
| Haskell | `timeout 5000000 (threadDelay 100000)` |
| Lua | `-- via socket` |
| Erlang | `receive after min(100, 5000) -> ok end.` |
| JavaScript | `await new Promise((r, j) => { const t = setTimeout(r, 100); setTimeout(() => { clearTimeout(t); j("timeout"); }, 5000); });` |
| Go | `select { case <-time.After(100 * time.Millisecond): case <-time.After(5000 * time.Millisecond): }` |
| Clojure | `(deref (future (Thread/sleep 100)) 5000 nil)` |

</details>

---

### `^ :stop` / `^ :pause` / `^ :resume` / `^ :delete` / `^ :reset`

**Control commands (result: agent status change).**

```true
^ :stop
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `raise SystemExit` |
| Rust | `return;` |
| Bash | `exit 0` |
| Elixir | `exit(:normal)` |
| C | `pthread_exit(NULL);` |
| Kotlin | `Thread.currentThread().interrupt()` |
| Haskell | `throwTo self ThreadKilled` |
| Lua | `coroutine.close(co)` |
| Erlang | `exit(self(), normal).` |
| JavaScript | `return;` |
| Go | `return` |
| Clojure | `(System/exit 0)` |

</details>

---

## <a name="simple"></a>11. Interpreter — `:simple`

**`:simple:*` — 21 interpreter functions, async. Result: `results["simple"] = "1"`. Destructive actions are logged. Path: absolute or relative (from `projectDir`, fallback `baseDir`).**

### `:simple:open`

**Opens a file.**

```true
/tmp/main.cycle => :simple:open
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `Popen(["code", "/tmp/main.cycle"])` |
| Rust | `open::that("/tmp/main.cycle")?;` |
| Bash | `xdg-open /tmp/main.cycle &` |
| Elixir | `System.cmd("xdg-open", ["/tmp/main.cycle"])` |
| C | `system("xdg-open /tmp/main.cycle &");` |
| Kotlin | `Desktop.getDesktop().open(File("/tmp/main.cycle"))` |
| Haskell | `callCommand "xdg-open /tmp/main.cycle &"` |
| Lua | `os.execute("xdg-open /tmp/main.cycle &")` |
| Erlang | `os:cmd("xdg-open /tmp/main.cycle &").` |
| JavaScript | `spawn("xdg-open", ["/tmp/main.cycle"]);` |
| Go | `exec.Command("xdg-open", "/tmp/main.cycle").Start()` |
| Clojure | `(clojure.java.shell/sh "xdg-open" "/tmp/main.cycle")` |

</details>

---

### `:simple:save`

**Saves the current file.**

```true
=> :simple:save
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `open(current_path, "w").write(current_content)` |
| Rust | `std::fs::write(current_path, current_content)?;` |
| Bash | `printf '%s' "$current_content" > "$current_path"` |
| Elixir | `File.write!(current_path, current_content)` |
| C | `FILE *f = fopen(current_path, "w"); fputs(current_content, f); fclose(f);` |
| Kotlin | `File(currentPath).writeText(currentContent)` |
| Haskell | `writeFile currentPath currentContent` |
| Lua | `local f = io.open(current_path, "w"); f:write(current_content); f:close()` |
| Erlang | `file:write_file(CurrentPath, CurrentContent).` |
| JavaScript | `writeFileSync(currentPath, currentContent);` |
| Go | `os.WriteFile(currentPath, []byte(currentContent), 0644)` |
| Clojure | `(spit current-path current-content)` |

</details>

---

### `:simple:saveAs`

**Saves the current file as a new path.**

```true
/tmp/new.cycle => :simple:saveAs
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `open("/tmp/new.cycle", "w").write(current_content)` |
| Rust | `std::fs::write("/tmp/new.cycle", current_content)?;` |
| Bash | `printf '%s' "$current_content" > /tmp/new.cycle` |
| Elixir | `File.write!("/tmp/new.cycle", current_content)` |
| C | `FILE *f = fopen("/tmp/new.cycle", "w"); fputs(current_content, f); fclose(f);` |
| Kotlin | `File("/tmp/new.cycle").writeText(currentContent)` |
| Haskell | `writeFile "/tmp/new.cycle" currentContent` |
| Lua | `local f = io.open("/tmp/new.cycle", "w"); f:write(current_content); f:close()` |
| Erlang | `file:write_file("/tmp/new.cycle", CurrentContent).` |
| JavaScript | `writeFileSync("/tmp/new.cycle", currentContent);` |
| Go | `os.WriteFile("/tmp/new.cycle", []byte(currentContent), 0644)` |
| Clojure | `(spit "/tmp/new.cycle" current-content)` |

</details>

---

### `:simple:newCycle`

**Creates a new cycle tab.**

```true
=> :simple:newCycle
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `current_tab = create_tab("new.cycle", default_code)` |
| Rust | `let tab = create_tab("new.cycle", &default_code);` |
| Bash | `touch new.cycle` |
| Elixir | `GenServer.cast(:ui, {:new_tab, "new.cycle"})` |
| C | `/* create new tab */` |
| Kotlin | `tabs.create("new.cycle", defaultCode)` |
| Haskell | `createTab "new.cycle" defaultCode` |
| Lua | `ui.new_tab("new.cycle")` |
| Erlang | `ui ! {new_tab, "new.cycle"}.` |
| JavaScript | `createTab("new.cycle", defaultCode);` |
| Go | `ui.CreateTab("new.cycle", defaultCode)` |
| Clojure | `(ui/create-tab "new.cycle" default-code)` |

</details>

---

### `:simple:newAgent`

**Creates a new agent tab.**

```true
=> :simple:newAgent
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `current_tab = create_tab("new.agent", default_code)` |
| Rust | `let tab = create_tab("new.agent", &default_code);` |
| Bash | `touch new.agent` |
| Elixir | `GenServer.cast(:ui, {:new_tab, "new.agent"})` |
| C | `/* create new agent tab */` |
| Kotlin | `tabs.create("new.agent", defaultCode)` |
| Haskell | `createTab "new.agent" defaultCode` |
| Lua | `ui.new_tab("new.agent")` |
| Erlang | `ui ! {new_tab, "new.agent"}.` |
| JavaScript | `createTab("new.agent", defaultCode);` |
| Go | `ui.CreateTab("new.agent", defaultCode)` |
| Clojure | `(ui/create-tab "new.agent" default-code)` |

</details>

---

### `:simple:openFolder`

**Opens a folder as a project.**

```true
/tmp/folder => :simple:openFolder
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `chdir("/tmp/folder"); load_files()` |
| Rust | `std::env::set_current_dir("/tmp/folder")?; load_files()?;` |
| Bash | `cd /tmp/folder` |
| Elixir | `File.cd!("/tmp/folder")` |
| C | `chdir("/tmp/folder");` |
| Kotlin | `File("/tmp/folder").listFiles()` |
| Haskell | `setCurrentDirectory "/tmp/folder"` |
| Lua | `-- no direct chdir` |
| Erlang | `file:set_cwd("/tmp/folder").` |
| JavaScript | `readdirSync("/tmp/folder");` |
| Go | `os.Chdir("/tmp/folder")` |
| Clojure | `(clojure.java.shell/sh "cd" "/tmp/folder")` |

</details>

---

### `:simple:rename`

**Renames the current file.**

```true
new-name.cycle => :simple:rename
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `rename(current_path, "new-name.cycle")` |
| Rust | `std::fs::rename(current_path, "new-name.cycle")?;` |
| Bash | `mv "$current_path" new-name.cycle` |
| Elixir | `File.rename!(current_path, "new-name.cycle")` |
| C | `rename(current_path, "new-name.cycle");` |
| Kotlin | `File(currentPath).renameTo(File("new-name.cycle"))` |
| Haskell | `renamePath currentPath "new-name.cycle"` |
| Lua | `os.rename(current_path, "new-name.cycle")` |
| Erlang | `file:rename(CurrentPath, "new-name.cycle").` |
| JavaScript | `renameSync(currentPath, "new-name.cycle");` |
| Go | `os.Rename(currentPath, "new-name.cycle")` |
| Clojure | `(clojure.java.io/rename-file current-path "new-name.cycle")` |

</details>

---

### `:simple:delete`

**Deletes the current file (logged).**

```true
=> :simple:delete
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `remove(current_path)` |
| Rust | `std::fs::remove_file(current_path)?;` |
| Bash | `rm -f "$current_path"` |
| Elixir | `File.rm!(current_path)` |
| C | `remove(current_path);` |
| Kotlin | `File(currentPath).delete()` |
| Haskell | `removeFile currentPath` |
| Lua | `os.remove(current_path)` |
| Erlang | `file:delete(CurrentPath).` |
| JavaScript | `unlinkSync(currentPath);` |
| Go | `os.Remove(currentPath)` |
| Clojure | `(clojure.java.io/delete-file current-path)` |

</details>

---

### `:simple:reset`

**Resets the current file.**

```true
=> :simple:reset
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `file.code = start_code; agent.current_step = 1` |
| Rust | `tab.code = start_code.clone(); agent.current_step = 1;` |
| Bash | `# reload from start` |
| Elixir | `file = %{file \| code: start_code}; agent = %{agent \| step: 1}` |
| C | `strcpy(tab.code, start_code); agent.current_step = 1;` |
| Kotlin | `tab.code = startCode; agent.currentStep = 1` |
| Haskell | `let tab' = tab { code = startCode, currentStep = 1 }` |
| Lua | `file.code = start_code; agent.current_step = 1` |
| Erlang | `Tab2 = Tab#tab{code = StartCode, step = 1}.` |
| JavaScript | `file.code = startCode; agent.currentStep = 1;` |
| Go | `tab.Code = startCode; agent.CurrentStep = 1` |
| Clojure | `(def tab' (assoc tab :code start-code :current-step 1))` |

</details>

---

### `:simple:close`

**Closes the current tab (logged).**

```true
=> :simple:close
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `close_tab(current_path)` |
| Rust | `close_tab(current_path);` |
| Bash | `# close tab` |
| Elixir | `GenServer.cast(:ui, {:close_tab, current_path})` |
| C | `/* close tab */` |
| Kotlin | `tabs.close(current)` |
| Haskell | `closeTab currentPath` |
| Lua | `ui.close_tab(current_path)` |
| Erlang | `ui ! {close_tab, CurrentPath}.` |
| JavaScript | `closeTab(currentPath);` |
| Go | `ui.CloseTab(currentPath)` |
| Clojure | `(ui/close-tab current-path)` |

</details>

---

### `:simple:moveUp` / `:simple:moveDown`

**Move the file up or down in `fileOrder`.**

```true
=> :simple:moveUp
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `file_order[idx], file_order[idx - 1] = file_order[idx - 1], file_order[idx]` |
| Rust | `file_order.swap(idx, idx - 1);` |
| Bash | `# swap array elements` |
| Elixir | `{first, [a, b \| rest]} = Enum.split(file_order, idx - 1); new = first ++ [b, a] ++ rest` |
| C | `char *tmp = file_order[idx]; file_order[idx] = file_order[idx - 1]; file_order[idx - 1] = tmp;` |
| Kotlin | `val tmp = fileOrder[idx]; fileOrder[idx] = fileOrder[idx - 1]; fileOrder[idx - 1] = tmp` |
| Haskell | `let (a, b:rest) = splitAt (idx - 1) fileOrder; let new = a ++ [rest !! 0, b] ++ drop 1 rest` |
| Lua | `file_order[idx], file_order[idx - 1] = file_order[idx - 1], file_order[idx]` |
| Erlang | `% manual list manipulation` |
| JavaScript | `[fileOrder[idx], fileOrder[idx - 1]] = [fileOrder[idx - 1], fileOrder[idx]];` |
| Go | `fileOrder[idx], fileOrder[idx-1] = fileOrder[idx-1], fileOrder[idx]` |
| Clojure | `(def new (assoc file-order idx (file-order (dec idx)) (dec idx) (file-order idx)))` |

</details>

---

### `:simple:startAll` / `:simple:pauseAll` / `:simple:stopAll` / `:simple:stepAll` / `:simple:stepBackAll`

**Control all agents.**

```true
=> :simple:startAll
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `for a in agents: a.start()` |
| Rust | `for a in agents.iter() { a.start(); }` |
| Bash | `for pid in "${agent_pids[@]}"; do kill -CONT "$pid"; done` |
| Elixir | `Enum.each(agents, &Agent.cast(&1, :start))` |
| C | `for (int i = 0; i < n; i++) agents[i].start();` |
| Kotlin | `agents.forEach { it.start() }` |
| Haskell | `mapM_ startAgent agents` |
| Lua | `for _, a in ipairs(agents) do a:start() end` |
| Erlang | `[gen_server:cast(A, start) \|\| A <- Agents].` |
| JavaScript | `agents.forEach(a => a.start());` |
| Go | `for _, a := range agents { a.Start() }` |
| Clojure | `(doseq [a agents] (.start a))` |

</details>

---

### `:simple:settings` / `:simple:about` / `:simple:samples` / `:simple:activeTab`

**Open dialogs or switch tab.**

```true
=> :simple:settings
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `show_settings_dialog()` |
| Rust | `ui.show_dialog("Settings");` |
| Bash | `# not idiomatic` |
| Elixir | `GenServer.cast(:ui, {:show_settings})` |
| C | `show_dialog("Settings");` |
| Kotlin | `showDialog("Settings")` |
| Haskell | `uiShowDialog "Settings"` |
| Lua | `ui.show_dialog("Settings")` |
| Erlang | `ui ! {show_dialog, "Settings"}.` |
| JavaScript | `openSettingsDialog();` |
| Go | `ui.ShowDialog("Settings")` |
| Clojure | `(ui/show-dialog "Settings")` |

</details>

---

## <a name="settings"></a>12. Settings

### `:historyLimit`

**Get or set the history limit (result in `results["historyLimit"]`).**

```true
:historyLimit
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `dst = str(history_limit)` |
| Rust | `let dst = history_limit.to_string();` |
| Bash | `dst=$history_limit` |
| Elixir | `dst = Integer.to_string(history_limit)` |
| C | `char dst[16]; sprintf(dst, "%d", history_limit);` |
| Kotlin | `val dst = historyLimit.toString()` |
| Haskell | `let dst = show historyLimit` |
| Lua | `local dst = tostring(history_limit)` |
| Erlang | `Dst = integer_to_list(HistoryLimit).` |
| JavaScript | `const dst = String(historyLimit);` |
| Go | `dst := strconv.Itoa(historyLimit)` |
| Clojure | `(def dst (str @history-limit))` |

</details>

---

### `:logLimit`

**Get or set the log limit (result in `results["logLimit"]`).**

```true
:logLimit
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `dst = str(log_limit)` |
| Rust | `let dst = log_limit.to_string();` |
| Bash | `dst=$log_limit` |
| Elixir | `dst = Integer.to_string(log_limit)` |
| C | `char dst[16]; sprintf(dst, "%d", log_limit);` |
| Kotlin | `val dst = logLimit.toString()` |
| Haskell | `let dst = show logLimit` |
| Lua | `local dst = tostring(log_limit)` |
| Erlang | `Dst = integer_to_list(LogLimit).` |
| JavaScript | `const dst = String(logLimit);` |
| Go | `dst := strconv.Itoa(logLimit)` |
| Clojure | `(def dst (str @log-limit))` |

</details>

---

### `:strictMode`

**Get or set strict mode (result in `results["strictMode"]`).**

```true
:strictMode
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `dst = "1" if strict_mode else "0"` |
| Rust | `let dst = if strict_mode { "1" } else { "0" };` |
| Bash | `dst=$strict_mode` |
| Elixir | `dst = if strict_mode, do: "1", else: "0"` |
| C | `const char *dst = strict_mode ? "1" : "0";` |
| Kotlin | `val dst = if (strictMode) "1" else "0"` |
| Haskell | `let dst = if strictMode then "1" else "0"` |
| Lua | `local dst = strict_mode and "1" or "0"` |
| Erlang | `Dst = case StrictMode of true -> "1"; false -> "0" end.` |
| JavaScript | `const dst = strictMode ? "1" : "0";` |
| Go | `dst := "0"; if strictMode { dst = "1" }` |
| Clojure | `(def dst (if @strict-mode "1" "0"))` |

</details>

---

### `:autoSync`

**Get or set auto-update of declarations (result in `results["autoSync"]`).**

```true
:autoSync
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `dst = "1" if auto_sync else "0"` |
| Rust | `let dst = if auto_sync { "1" } else { "0" };` |
| Bash | `dst=$auto_sync` |
| Elixir | `dst = if auto_sync, do: "1", else: "0"` |
| C | `const char *dst = auto_sync ? "1" : "0";` |
| Kotlin | `val dst = if (autoSync) "1" else "0"` |
| Haskell | `let dst = if autoSync then "1" else "0"` |
| Lua | `local dst = auto_sync and "1" or "0"` |
| Erlang | `Dst = case AutoSync of true -> "1"; false -> "0" end.` |
| JavaScript | `const dst = autoSync ? "1" : "0";` |
| Go | `dst := "0"; if autoSync { dst = "1" }` |
| Clojure | `(def dst (if @auto-sync "1" "0"))` |

</details>

---

## <a name="meta"></a>13. Meta

### `:run`

**Executes a line atomically. Without argument — the current line.**

```true
:run
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `exec(current_line)` |
| Rust | `// no direct eval` |
| Bash | `eval "$current_line"` |
| Elixir | `Code.eval_string(current_line)` |
| C | `/* no direct eval */` |
| Kotlin | `// no direct eval` |
| Haskell | `-- via hint` |
| Lua | `load(current_line)()` |
| Erlang | `erl_eval:exprs(CurrentLine, []).` |
| JavaScript | `eval(currentLine);` |
| Go | `// no direct eval` |
| Clojure | `(eval (read-string current-line))` |

</details>

---

### `:log`

**Logs a line. Without argument — logs the current line.**

```true
:log
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `print(current_line)` |
| Rust | `println!("{}", current_line);` |
| Bash | `echo "$current_line"` |
| Elixir | `IO.puts(current_line)` |
| C | `printf("%s\n", current_line);` |
| Kotlin | `println(currentLine)` |
| Haskell | `putStrLn currentLine` |
| Lua | `print(current_line)` |
| Erlang | `io:format("~s~n", [CurrentLine]).` |
| JavaScript | `console.log(currentLine);` |
| Go | `fmt.Println(currentLine)` |
| Clojure | `(println current-line)` |

</details>

---

### `:message`

**Writes a message to the log with type `message` (result in `results["message"]`).**

```true
hello => :message \ py: `log_message("hello")` | elixir: `Logger.info("hello")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `log_message("hello")` |
| Rust | `log::info!("hello");` |
| Bash | `echo "hello"` |
| Elixir | `Logger.info("hello")` |
| C | `printf("hello\n");` |
| Kotlin | `logMessage("hello")` |
| Haskell | `putStrLn "hello"` |
| Lua | `print("hello")` |
| Erlang | `io:format("~s~n", ["hello"]).` |
| JavaScript | `logMessage("hello");` |
| Go | `log.Println("hello")` |
| Clojure | `(println "hello")` |

</details>

---

### `:result`

**Stores the value as the function result (result in `results["result"]`).**

```true
42 => :result
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `result = 42` |
| Rust | `let result = 42;` |
| Bash | `result=42` |
| Elixir | `result = 42` |
| C | `int result = 42;` |
| Kotlin | `val result = 42` |
| Haskell | `let result = 42` |
| Lua | `local result = 42` |
| Erlang | `Result = 42.` |
| JavaScript | `result = 42;` |
| Go | `result := 42` |
| Clojure | `(def result 42)` |

</details>

---

### `:parameters`

**Returns the entry argument of a file-function (result in `results["parameters"]`).**

```true
:parameters
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `p = sys.argv[1]` |
| Rust | `let p = std::env::args().nth(1);` |
| Bash | `p=$1` |
| Elixir | `p = System.argv() \|> Enum.at(0)` |
| C | `const char *p = argc > 1 ? argv[1] : "";` |
| Kotlin | `val p = args.getOrNull(0)` |
| Haskell | `args <- getArgs; let p = if null args then "" else head args` |
| Lua | `local p = arg[1] or ""` |
| Erlang | `P = hd(init:get_plain_arguments()).` |
| JavaScript | `const p = process.argv[2];` |
| Go | `p := ""; if len(os.Args) > 1 { p = os.Args[1] }` |
| Clojure | `(def p (first *command-line-args*))` |

</details>

---

### `:range`

**Generates a range of numbers (result in `results["range"]`).**

```true
1, 5 => :range \ py: `out = ",".join(str(n) for n in range(1, 6))` | elixir: `out = Enum.join(1..5, ",")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = ",".join(str(n) for n in range(1, 6))` |
| Rust | `let out = (1..=5).map(\|n\| n.to_string()).collect::<Vec<_>>().join(",");` |
| Bash | `out=$(seq -s, 1 5)` |
| Elixir | `out = Enum.join(1..5, ",")` |
| C | `/* manual assembly */` |
| Kotlin | `val out = (1..5).joinToString(",")` |
| Haskell | `let out = intercalate "," (map show [1..5])` |
| Lua | `local t = {}; for i = 1, 5 do table.insert(t, tostring(i)) end; local out = table.concat(t, ",")` |
| Erlang | `Out = string:join([integer_to_list(N) \|\| N <- lists:seq(1, 5)], ",").` |
| JavaScript | `const out = [...Array(5).keys()].map(n => n + 1).join(",");` |
| Go | `parts := []string{}; for i := 1; i <= 5; i++ { parts = append(parts, strconv.Itoa(i)) }; out := strings.Join(parts, ",")` |
| Clojure | `(def out (clojure.string/join "," (range 1 6)))` |

</details>

---

### `:repeat`

**Executes a named action N times.**

```true
0 = a
a + 1 => a = action
action, 5 => :repeat
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = 0; exec("a += 1") for _ in range(5)` |
| Rust | `let mut a = 0; for _ in 0..5 { a += 1; }` |
| Bash | `a=0; for i in $(seq 1 5); do a=$((a + 1)); done` |
| Elixir | `a = Enum.reduce(1..5, 0, fn _, acc -> acc + 1 end)` |
| C | `int a = 0; for (int i = 0; i < 5; i++) a++;` |
| Kotlin | `var a = 0; repeat(5) { a += 1 }` |
| Haskell | `let a = foldl (\acc _ -> acc + 1) 0 [1..5]` |
| Lua | `local a = 0; for i = 1, 5 do a = a + 1 end` |
| Erlang | `A = lists:foldl(fun(_, Acc) -> Acc + 1 end, 0, lists:seq(1, 5)).` |
| JavaScript | `let a = 0; for (let i = 0; i < 5; i++) a += 1;` |
| Go | `a := 0; for i := 0; i < 5; i++ { a++ }` |
| Clojure | `(def a (reduce (fn [acc _] (inc acc)) 0 (range 5)))` |

</details>

---

### `:activeTab`

**Switches the active tab (1-based) and optionally the step.**

```true
1 => :activeTab
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `tabs.select(0)` |
| Rust | `tabs.select(0);` |
| Bash | `# not idiomatic` |
| Elixir | `GenServer.cast(:ui, {:select_tab, 0})` |
| C | `select_tab(0);` |
| Kotlin | `tabs.select(0)` |
| Haskell | `selectTab 0` |
| Lua | `ui.select_tab(1)` |
| Erlang | `ui ! {select_tab, 1}.` |
| JavaScript | `tabs.select(0);` |
| Go | `ui.SelectTab(0)` |
| Clojure | `(ui/select-tab 0)` |

</details>

---

### `:size` / `:s`

**Returns size by axis (result in `results["size"]`).**

```true
hello => :s \ py: `out = len("hello")` | rust: `let out = "hello".len();`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = len("hello")` |
| Rust | `let out = "hello".len();` |
| Bash | `out=${#s}` |
| Elixir | `out = String.length("hello")` |
| C | `int out = strlen("hello");` |
| Kotlin | `val out = "hello".length` |
| Haskell | `let out = length "hello"` |
| Lua | `local out = #"hello"` |
| Erlang | `Out = length("hello").` |
| JavaScript | `const out = "hello".length;` |
| Go | `out := len("hello")` |
| Clojure | `(def out (count "hello"))` |

</details>

