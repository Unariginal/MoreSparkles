## ✨ More Sparkles ✨
More Sparkles is a server-side mod for Cobblemon 1.8.0 (Minecraft 1.21.1) that lets you boost shiny odds, experience, IVs, Alpha spawns and more!

### ✨ Features ✨
- 10 boost types: Shiny, Experience, Hidden Ability, EV, IV, Berry, Catch Rate, Mark, Alpha and Spawn Bucket
- Player-based boosts
- Global boosts
- Boost queues, kept separately for each boost type
- Area-based boosts (cuboid & cylinder-shaped areas)
- 10 charm items added through Polymer, one for each boost type
- Custom charm items that you add through the config
- Discord webhook embeds that show active boosts
- Placeholder API support (optional)

### ✨ Boost Types ✨
| Type | What it boosts |
|---|---|
| `SHINY` | Shiny odds |
| `EXPERIENCE` | Experience gained in battle |
| `HIDDEN_ABILITY` | Chance that wild spawns have their Hidden Ability |
| `EV` | EVs gained |
| `IV` | IV quality of Pokémon you catch, hatch or revive from fossils |
| `BERRY` | Berry harvest yield |
| `CATCH_RATE` | Catch rate |
| `MARK` | Chance of a mark on Pokémon you catch, hatch or revive from fossils |
| `ALPHA` | Alpha spawn chance |
| `SPAWN_BUCKET` | Odds of uncommon, rare and ultra-rare spawn buckets |

### ✨ Charms ✨
- Shiny Charm (Shiny)
- Exp Charm (Experience)
- Veiled Charm (Hidden Ability)
- Effort Charm (EV)
- IV Charm (IV)
- Berry Charm (Berry)
- Catching Charm (Catch Rate)
- Mark Charm (Mark)
- Alpha Charm (Alpha)
- Bucket Charm (Spawn Bucket)

### ✨ Commands ✨
Base Command:
- /sparkles

Sub Commands:
- /sparkles boost start <type> <multiplier> <duration> seconds/minutes/hours/days global/<player(s)>
- /sparkles boost stop <type> global/<player(s)>
- /sparkles boost status [global/<player>]
- /sparkles check-queue [global/<player>]
- /sparkles clear-queue global/<player(s)>
- /sparkles check-rate <type> [<player>]
- /sparkles area info [<area-id>]
- /sparkles reload

### ✨ Permissions ✨
| Permission | Default |
|---|---|
| `sparkles.boost` | Everyone |
| `sparkles.boost.start` | OP |
| `sparkles.boost.stop` | OP |
| `sparkles.boost.status` | Everyone |
| `sparkles.boost.status.global` | Everyone |
| `sparkles.boost.status.others` | OP |
| `sparkles.checkqueue` | Everyone |
| `sparkles.checkqueue.global` | OP |
| `sparkles.checkqueue.others` | OP |
| `sparkles.clearqueue` | OP |
| `sparkles.checkrate` | Everyone |
| `sparkles.checkrate.others` | OP |
| `sparkles.area` | OP |
| `sparkles.area.info` | OP |
| `sparkles.reload` | OP |

### ✨ Placeholders ✨
When [Placeholder API](https://modrinth.com/mod/placeholder-api) is installed, these placeholders are registered under the `moresparkles:` namespace:

Server:
- `base_shiny_rate`, `base_bucket_chance`, `base_alpha_bucket_chance`

Player:
- `player_shiny_rate`, `player_hidden_ability_chance`, `player_iv_strength`, `player_bucket_chance`, `player_alpha_bucket_chance`
- `player_<type>_multiplier` for each boost type, using the lowercased type name (e.g. `player_shiny_multiplier`, `player_spawn_bucket_multiplier`)

### ✨ Configs ✨
All files are in `config/MoreSparkles/`. Missing keys are filled in with their defaults automatically.

config.json
- General boost settings: queues, pausing boosts, how the Experience/EV/Hidden Ability/IV boosts behave, and the Discord webhook settings

boost_areas.json
- This is where you can define boost areas, each with its own boost type, multiplier and display name. Examples are available in the Wiki

messages.json
- This contains all the command feedback / general messages, along with bossbar design settings

items.json
- This is where you can set each charm's boost type, multiplier and lore, and add your own custom charms

global_boosts.json
- This stores the active and queued global boosts, so they persist through restarts

players/<player-uuid>.json
- This is where each player's boost data is stored, so it persists through restarts
