# Libb
A library for convenient and easy creation of Minecraft plugins.

---

### Requirements:
##### Java: 17
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
  <url>http://api.jetby.space/</url>
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
        url "http://api.jetby.space/"
        name "jetby-repo"
    }
}
```
```gradle
dependencies {
    compileOnly "me.jetby:Libb:1.0"
}
```
