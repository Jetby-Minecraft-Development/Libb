# Libb
A library for convenient and easy creation of Minecraft plugins.

---

### Requirements:
##### Java: 17
##### Paper
##### Minecraft version: 1.20 and higher

---

<details>
<summary>GUI</summary>

### Example

```java
public class GuiTest extends AdvancedGui {
    public GuiTest() {
        super("Gui title");
        setItem("example", ItemWrapper.builder(Material.STONE)
                .slots(1, 5, 7)
                .displayName(Component.text("This is the name dude"))
                .onClick(event -> {
                    event.setCancelled(true);
                    player.sendMessage("Clicked on slot: " + event.getSlot());
                })
                .build());
    }
}
```

To open a GUI for a player, call `open()`:

```java
new GuiTest().open(player);
```

</details>

---

<details>
<summary>ParsedGui — Config-driven GUIs</summary>

`ParsedGui` lets you define an entire inventory GUI in a YAML config file — items, slots, click actions, open/close hooks, and placeholders — with no boilerplate code.

---

### YAML structure

```yaml
id: my_gui
title: "<gold>My Shop"
size: 54          # must be a multiple of 9

on_open:          # optional — action list to run when GUI opens
  - "[sound] UI_BUTTON_CLICK;1;1"

on_close:         # optional — action list to run when GUI closes
  - "[message] <gray>Closed the shop."

Items:
  my_item:
    material: DIAMOND
    slot: 13                    # single slot
    display_name: "<aqua>Buy Diamond"
    lore:
      - ""
      - " <gray>Click to purchase"
      - ""
    on_click:
      any:                      # fires on every click type
        - "[sound] UI_BUTTON_CLICK;1;1"
        - "[player] buy diamond"
      left:                     # fires only on left click
        - "[message] <green>Left clicked!"
      shift_left:
        - "[message] <yellow>Shift+Left!"
```

**Supported click types:** `any`, `left`, `shift_left`, `right`, `shift_right`, `middle`, `drop`, `control_drop`, `double`

**Slot formats:**

```yaml
slot: 13              # single slot
slots:
  - '0-8'             # range
  - '45-53'           # another range
  - '27'              # single inside a list
```

---

### Opening from code

```java
// From a FileConfiguration:
FileConfiguration config = YamlConfiguration.loadConfiguration(file);
new ParsedGui(player, config, myPlugin).open(player);

// From a pre-parsed Gui record (more efficient for many players):
Gui gui = ...; // parsed once at startup
new ParsedGui(player, gui, myPlugin).open(player);
```

---

### Runtime placeholders

Use `setReplace()` to inject values into display names, lore, and action lines at runtime.
Call it **before** `open()` — items are built on open.

```java
ParsedGui gui = new ParsedGui(player, config, myPlugin);
gui.setReplace("%price%", "500")
   .setReplace("%item_name%", "Diamond Sword");
gui.open(player);
```

In YAML:
```yaml
display_name: "<white>Price: <green>$%price%"
lore:
  - " <gray>Item: <white>%item_name%"
```

PlaceholderAPI placeholders (`%papi_placeholder%`) are applied automatically — no extra setup needed.

---

### Click handlers from code

Register Java-side click logic for items by their YAML section key.
Runs **in addition** to whatever `on_click` is defined in YAML.

```java
ParsedGui gui = new ParsedGui(player, config, myPlugin);

gui.addClickHandler("my_item", event -> {
    Player clicker = (Player) event.getWhoClicked();
    clicker.sendMessage("You clicked my_item!");
    gui.refresh();
});

gui.open(player);
```

---

### Passing the GUI into actions via ActionContext

When a player clicks an item, `ParsedGui` puts itself into the `ActionContext` automatically.
Inside a custom action you can retrieve it:

```java
ActionRegistry.register("myplugin", "my_action", (ctx, text) -> {
    ParsedGui gui = ctx.get(ParsedGui.class);
    if (gui == null) return;

    // do something, then refresh
    gui.refresh();
});
```

In config:
```yaml
on_click:
  any:
    - "[myplugin:my_action]"
```

---

### Slot priority & view_requirements

Multiple items can target the same slot. The one with the lowest `priority` value whose `view_requirements` all pass wins.
This is useful for conditional items — e.g. show a locked version until the player has enough money.

```yaml
Items:
  buy_locked:
    material: RED_STAINED_GLASS_PANE
    slot: 13
    priority: 1
    display_name: "<red>Not enough money"
    view_requirements:
      - "%vault_eco_balance% < 100"   # shown when balance < 100

  buy_unlocked:
    material: EMERALD
    slot: 13
    priority: 2                        # fallback — shown when locked item's requirement fails
    display_name: "<green>Buy"
```

`view_requirements` supports `==`, `!=`, `>=`, `<=`, `>`, `<` with both numbers and strings.
PlaceholderAPI placeholders are resolved before comparison.

---

### Refreshing the GUI

Call `refresh()` to clear and rebuild all items — re-evaluates `view_requirements` and re-applies all placeholders.

```java
gui.refresh();
```

Typically called inside a click handler after state changes:
```java
gui.addClickHandler("toggle", event -> {
    toggleSomething(player);
    gui.refresh();
});
```

---

### Extending ParsedGui

You can subclass `ParsedGui` to add custom inventory slots, override rendering logic, etc.

> ⚠️ `super(viewer, config, plugin)` calls `buildItems()` internally during construction — before your subclass fields are initialized. Override `buildItems()` with a null-check guard:

```java
public class MyGui extends ParsedGui {

    private final MyPlugin plugin;

    public MyGui(Player viewer, FileConfiguration config, MyPlugin plugin) {
        super(viewer, config, plugin);
        this.plugin = plugin;
        // your init here
    }

    @Override
    public void buildItems(List<Item> items) {
        if (plugin == null) {          // guard: called from super() before our fields exist
            super.buildItems(items);
            return;
        }
        // your custom logic, then:
        super.buildItems(items);
    }

    @Override
    public void refresh() {
        // update your replacements before items are rebuilt
        setReplace("%score%", String.valueOf(getScore()));
        super.refresh();
    }
}
```

</details>

---

<details>
<summary>Actions</summary>

Actions are config-driven commands executed on a player. Each action is a string in the format `[key] text`.

### Built-in actions

| Key | Description |
|-----|-------------|
| `[message]` | Send a message to the player |
| `[broadcast_message]` | Broadcast a message to all players |
| `[console]` | Run a command from console |
| `[player]` | Run a command as the player |
| `[effect]` | Apply a potion effect |
| `[action_bar]` | Send an action bar message |
| `[broadcast_action_bar]` | Broadcast an action bar to all players |
| `[title]` | Send a title to the player |
| `[broadcast_title]` | Broadcast a title to all players |
| `[sound]` | Play a sound for the player |
| `[broadcast_sound]` | Play a sound for all players |
| `[open]` | Open a GUI |

### Usage

**Simple run:**
```java
ActionExecute.run(ActionContext.of(player), "[message] Hello!");
```

**With extra objects in context:**
```java
ActionExecute.run(
    ActionContext.of(player).with(entity),
    "[myplugin:give_diamond] 64"
);
```

> Extra objects added via `.with()` can be retrieved inside the handler using `ctx.get(YourClass.class)`.

---

### Registering a custom action

Custom actions are registered in `onEnable` and unregistered in `onDisable`.

Actions from different plugins can share the same key without conflict — the full key is `namespace:command`:

```yaml
# config.yml
actions:
  - "[plugina:spawn] text"      # resolves plugina's spawn
  - "[pluginb:spawn] text"      # resolves pluginb's spawn — no conflict
  - "[spawn] text"              # resolves whichever was registered first
```

#### Lambda (simple cases)

```java
@Override
public void onEnable() {
    ActionRegistry.register("myplugin", "give_diamond", (ctx, text) -> {
        Player player = ctx.getPlayer();
        if (player == null || text == null) return;

        int amount = Integer.parseInt(text);
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, amount));
        player.sendMessage("You got " + amount + " diamonds!");
    });
}

@Override
public void onDisable() {
    ActionRegistry.unregisterAll("myplugin");
}
```

#### Class (recommended for complex logic)

Register in `onEnable`:
```java
@Override
public void onEnable() {
    ActionRegistry.register("myplugin", "give_diamond", new GiveDiamondAction());
}

@Override
public void onDisable() {
    ActionRegistry.unregisterAll("myplugin");
}
```

Implement `Action`:
```java
public class GiveDiamondAction implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @Nullable String text) {
        Player player = ctx.getPlayer();
        if (player == null || text == null) return;

        // Parse text argument
        int amount = Integer.parseInt(text);
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, amount));
        player.sendMessage("You got " + amount + " diamonds!");

        // Retrieve a custom object from context — null if not provided
        Entity entity = ctx.get(Entity.class);
        if (entity == null) return;

        entity.teleport(player.getLocation());
        ActionExecute.run(
            ActionContext.of(player),
            "[message] <red>Entity has been teleported to you"
        );
    }
}
```

#### ActionContext

`ActionContext` is a type-safe container for objects passed into an action. Objects are stored and retrieved by class — no string keys needed.

```java
// Put objects in
ActionContext ctx = ActionContext.of(player)
        .with(entity)       // store by entity.getClass()
        .with(myGui);       // store by myGui.getClass()

// Get objects out (inside a handler)
Entity entity = ctx.get(Entity.class);      // null if not provided
Entity entity = ctx.require(Entity.class);  // throws if not provided
```

If you want to store an object under an interface rather than its concrete class:
```java
ctx.with(MyInterface.class, myObject);
// retrieve as:
ctx.get(MyInterface.class);
```

</details>

# API
###### MAVEN
```xml
<repository>
  <id>jetby-repo</id>
  <url>https://api.jetby.org/</url>
</repository>
```
```xml
<dependency>
  <groupId>me.jetby</groupId>
  <artifactId>Libb</artifactId>
  <version>1.0</version>
  <scope>provided</scope>
</dependency>
```
###### GRADLE
```gradle
repositories {
    maven {
        url "https://api.jetby.org/"
        name "jetby-repo"
    }
}
```
```gradle
dependencies {
    compileOnly "me.jetby:Libb:1.0"
}
```
