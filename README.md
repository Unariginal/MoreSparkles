## ✨ More Sparkles ✨
More Sparkles is a mod for Cobblemon 1.6.1 that adds multiple methods of Shiny Boosting!

### ✨ Features ✨
- Player-based shiny boosts
- Global shiny boosts
- Shiny boost queues
- Area-based shiny boosts (cuboid & cylinder-shaped areas)
- A customizable shiny charm item added through Polymer
- Option to add custom shiny charm items through the config

### ✨ Commands ✨
Base Command:
- /sparkles

Sub Commands:
- /sparkles boost start global/<player(s)> <multiplier> <duration> seconds/minutes/hours/days
- /sparkles boost stop global/<player(s)>
- /sparkles boost status [global/<player>]
- /sparkles check-queue [global/<player>]
- /sparkles check-rate [<player>]
- /sparkles area info [<area-id>]
- /sparkles reload

### ✨ Configs ✨
config.json
- This contains a few settings for boosts, and it stores the current and queued global boosts to persist through restarts

boost_areas.json
- This is where you can define shiny boost areas, examples available in the Wiki

messages.json
- This contains all the command feedback / general messages, along with bossbar design settings

items.json
- This is where you can define shiny charm settings and add custom shiny charms

playerdata/<player-uuid>.json
- This is where your player's shiny boost data is stored, so it persists through restarts
