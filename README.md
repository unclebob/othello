# Othello

Player vs computer Othello in Clojure and [Quil](http://quil.info).

You are Black by default. Click a highlighted square. The computer answers with
negamax, alpha-beta pruning, and timed iterative deepening (about 1.2s per move,
exact search when 12 or fewer squares remain).

## Run

Needs Clojure CLI and Java 21+.

```bash
clj -M:run
```

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
clj -M:spec      # Speclj
clj -M:crap      # CRAP report (coverage + complexity)
clj -M:mutate src/othello/game.clj --max-workers 3
```

Mutation testing is one file at a time via [clj-mutate](https://github.com/unclebob/clj-mutate).
Domain and UI-logic namespaces were driven to a 100% kill rate.

## Layout

```
src/othello/
  board.clj          64-square board
  rules.clj          flips, legal moves, winner
  game.clj           turn, pass, undo, game over
  ai.clj             computer entry point
  ai/eval.clj        positional + mobility evaluation
  ai/search.clj      negamax / alpha-beta / iterative deepening
  ui/layout.clj      geometry and hit testing
  ui/view.clj        view-model (no Quil)
  ui/events.clj      clicks, keys, animation, computer-turn protocol
  ui/draw.clj        Quil painting
  ui/sketch.clj      window and async AI
  core.clj           -main
```

The domain does not depend on Quil. Only `draw` and `sketch` talk to Processing.
`spec/othello/architecture_spec.clj` enforces that.
