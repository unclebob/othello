# Othello

Player vs computer Othello in Clojure, ClojureScript, and [Quil](http://quil.info).

You are Black by default. Click a highlighted square. The computer answers with
negamax, alpha-beta pruning, and timed iterative deepening.

## Run on the desktop

Needs Clojure CLI and Java 21+.

```bash
clj -M:run
```

Desktop search budget is about 1.2s per move, with exact search at 12 empties.

## Play in the browser

```bash
clj -M:web watch app
```

Then open [http://localhost:8080/index.html](http://localhost:8080/index.html).
The browser build uses the same rules and UI logic; the computer thinks a bit
less deeply so the page stays responsive.

## Play

- Click a dotted square to move.
- Hover shows a ghost disc.
- The computer thinks, then flips with the same animation you do.

| Key | Action |
|-----|--------|
| `N` | New game |
| `U` | Undo last turn (you + computer) |
| `H` | Toggle move hints |
| `1` | Play Black (you move first) |
| `2` | Play White (computer opens) |

Sidebar buttons do the same things.

## Tests

```bash
clj -M:spec      # Speclj (JVM, against the shared .cljc sources)
clj -M:crap      # CRAP report (coverage + complexity)
clj -M:mutate src/othello/game.cljc --max-workers 3
```

Mutation testing is one file at a time via [clj-mutate](https://github.com/unclebob/clj-mutate).

## Layout

```
src/othello/
  board.cljc         64-square board
  rules.cljc         flips, legal moves, winner
  game.cljc          turn, pass, undo, game over
  ai.cljc            computer entry point
  ai/eval.cljc       positional + mobility evaluation
  ai/search.cljc     negamax / alpha-beta / iterative deepening
  ui/layout.cljc     geometry and hit testing
  ui/anim.cljc       flip/think/pass frame policy
  ui/view.cljc       view-model (no Quil)
  ui/events.cljc     clicks, keys, animation, computer-turn protocol
  ui/host.cljc       shared fun-mode loop and input
  ui/draw.cljc       Quil painting (JVM Processing or browser p5)
  ui/sketch.clj      desktop window and async AI
  ui/web.cljs        browser sketch
  core.clj           desktop -main
```

The domain does not depend on Quil. Only `draw`, `sketch`, and `web` talk to
Processing/p5. `spec/othello/architecture_spec.clj` enforces that.

## Object model

Namespaces map to classes; the UI session is the one object with identity.
Color is mean function CRAP (`clj -M:crap`); labels are μ / max / σ.
Class diagrams and a turn sequence live in [`public/uml.html`](public/uml.html).

```mermaid
flowchart TB
  subgraph adapters ["Adapters  μ 3.1  max 5.8  σ 1.7"]
    direction LR
    DesktopApp ~~~ BrowserApp ~~~ Painter ~~~ FutureJob ~~~ TimeoutJob
  end
  subgraph app ["UI application  μ 2.1  max 9.0  σ 1.5"]
    direction LR
    Host ~~~ Session ~~~ ViewModel ~~~ Layout ~~~ AnimationPolicy
  end
  subgraph aiLayer ["AI  μ 2.2  max 4.0  σ 1.3"]
    direction LR
    Player ~~~ SearchPlayer ~~~ Evaluator
  end
  subgraph domain ["Domain  μ 1.7  max 5.0  σ 1.1"]
    direction LR
    Game ~~~ Board ~~~ Rules
  end
  adapters --> app
  app --> aiLayer
  app --> domain
  classDef low fill:#1e4a38,stroke:#5fb58a,color:#e8f5ee
  classDef mid fill:#3d3a18,stroke:#d4c05a,color:#f5f0d8
  classDef high fill:#4a2818,stroke:#e07a4a,color:#f8e4d8
  classDef na fill:#24302c,stroke:#6a7e76,color:#9db8a8
  class Game,Board,Rules,Host,Layout,AnimationPolicy,Player low
  class Session,ViewModel,SearchPlayer,Evaluator mid
  class DesktopApp,Painter,FutureJob high
  class BrowserApp,TimeoutJob na
  style adapters fill:#2a1610,stroke:#e07a4a,color:#e8c448
  style app fill:#242010,stroke:#d4c05a,color:#e8c448
  style aiLayer fill:#242010,stroke:#d4c05a,color:#e8c448
  style domain fill:#10241c,stroke:#5fb58a,color:#e8c448
```
