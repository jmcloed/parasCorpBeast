package org.example;

import com.google.common.eventbus.Subscribe;
import kotlin.Pair;
import org.powbot.api.*;
import org.powbot.api.event.MessageEvent;
import org.powbot.api.rt4.*;
import org.powbot.api.rt4.Objects;
import org.powbot.api.rt4.walking.model.Skill;
import org.powbot.api.script.*;

import java.util.*;

import static org.powbot.api.rt4.Prayer.prayer;

@ScriptManifest(name = "Corp Killer", description = "Kills Corporeal Beast", version = "1.0", author = "Highlander",
category = ScriptCategory.MoneyMaking)
// TODO: finish setting up menu
public class parasCorpBeast extends AbstractScript {
    private static Npc CB;
    private static Npc DEC;
    public static int bossKills = 0;
    public static boolean corpAlive = false;
    public static int dragonHammerHits = 0;  //require 3
    public static int arclightHits = 0;  //require 20
    public static int bandosDamage = 0;  //require 200
    public static long currentExp;
    public static long newExp;
    // TODO: create method to check bank for alternative gear
    public static final String spec1_MAIN_HAND = "Dragon warhammer";
    public static final String OFF_HAND = "Dragon defender";
    public static final String spec1_HEAD = "Inquisitor's great helm";
    public static final String spec1_RING = "Tyrannical ring (i)";
    public static final String spec1_CAPE = "Mythical cape";
    public static final String spec2_MAIN_HAND = "Arclight";
    public static final String spec2_HEAD = "Warrior helm";
    public static final String spec2_RING = "Bellator ring";
    public static final String spec3_MAIN_HAND = "Bandos godsword";
    public static final String spec3_HEAD = "Torva full helm";
    public static final String spec3_RING = "Ultor ring";
    public static final String spec3_CAPE = "Fire cape";
    public static final String spec4_MAIN_HAND = "Zamorakian spear";
    public static final String RUNE_POUCH = "Rune pouch";
    public static final String NECK = "Amulet of torture";
    public static final String TORSO = "Masori body (f)";
    public static final String LEGS = "Masori chaps (f)";
    public static final String HANDS = "Ferocious gloves";
    public static final String FEET = "Primordial boots";
    public static final String QUIVER = "Honourable blessing";
    public static final String corpPet = "Dark core";
    public static String[] wantedItems = { "Spectral sigil", "Arcane sigil", "Elysian sigil", "Onyx bolts (e)", "Cannonball",
            "Mystic robe top", "Mystic robe bottom", "Mystic air staff", "Mystic water staff", "Mystic earth staff",
            "Mystic fire staff", "Spirit shield", "Soul rune", "Runite bolts", "Death rune", "Adamant arrow",
            "Law rune", "Cosmic rune", "Raw shark", "Pure essence", "Adamantite bar", "Green dragonhide", "Teak plank",
            "Adamantite ore", "Runite ore", "Mahogany logs", "Magic logs", "Tuna potato", "White berries",
            "Desert goat horn", "Watermelon seed", "Coins", "Antidote++(4)", "Ranarr seed", "Holy elixir", "Jar of spirits",
            "Clue scroll (elite)", "Dark core" };
    public enum Location {
        POH, CORPOREAL_BEAST_ENTRANCE, CORPOREAL_BEAST_ROOM, GRAND_EXCHANGE, UNKNOWN
    }
    public enum Phase {
        Phase1, Phase2, Phase3, Phase4
    }
    public static final List<String> requiredItems = Arrays.asList(spec1_MAIN_HAND, OFF_HAND,
            spec1_HEAD, spec1_RING, spec1_CAPE, spec2_MAIN_HAND, spec2_HEAD, spec2_RING, spec3_MAIN_HAND,
            spec3_HEAD, spec3_RING, spec3_CAPE, spec4_MAIN_HAND, RUNE_POUCH, NECK, TORSO, LEGS, HANDS, FEET, QUIVER);
    public enum GearPhase {
        PhaseOneGear, PhaseTwoGear, PhaseThreeGear, PhaseFourGear, Unknown
    }
    public static Location getCurrentLocation() {
        if (Objects.stream().name("Ornate jewellery box").findFirst().orElse(null) != null) {
            return Location.POH;
        } else if (Objects.stream().name("Bank booth").findFirst().orElse(null) != null) {
            return Location.GRAND_EXCHANGE;
        } else if (Players.local().x() >= 2974 && Players.local().x() <= 3000 && Players.local().y() >= 4370 && Players.local().y() <= 4398) {
            return Location.CORPOREAL_BEAST_ROOM;
        } else if (Players.local().x() >= 2963 && Players.local().x() <= 2972 && Players.local().y() >= 4378 && Players.local().y() <= 4388) {
            return Location.CORPOREAL_BEAST_ENTRANCE;
        } else {
            return Location.UNKNOWN;
        }
    }
    public static void teleportToPOH() {
        System.out.println("(teleportToPOH) -- Teleporting to POH");
        Magic.Spell.TELEPORT_TO_HOUSE.cast("Cast");
        Condition.wait(Magic.Spell.TELEPORT_TO_HOUSE::casting, 50, 100);
    }
    public static void teleportToGE() {
        System.out.println("(teleportToGE) -- Teleporting to GE");
        GameObject jewelleryBox = Objects.stream().name("Ornate Jewellery Box").first();
        // Check if the object is found and in viewport.
        if (jewelleryBox.valid() && jewelleryBox.inViewport()) {
            System.out.println("(teleportToGE) -- Found jewellery box, interacting with ornate jewellery box...");
            // Interact with the Ornate Jewellery Box.
            if (jewelleryBox.interact("Teleport Menu")) {
                // Wait for the teleport menu to load.
                Condition.wait(() -> Components.stream().widget(590).text("Grand Exchange").viewable().isNotEmpty(), 1000, 10);
                // Now interact with the Grand Exchange option.
                Component geComponent = Components.stream().widget(590).text("Grand Exchange").viewable().first();
                if (geComponent.valid()) {
                    geComponent.click();
                    // Wait until the player is at the Grand Exchange
                    boolean arrivedAtGE = Condition.wait(() -> {
                        Location currentLocation = getCurrentLocation();
                        return currentLocation == Location.GRAND_EXCHANGE;
                    }, 1000, 10);
                    if (arrivedAtGE) {
                        System.out.println("(teleportToGE) -- Successfully teleported to the Grand Exchange.");
                    } else {
                        System.out.println("(teleportToGE) -- Failed to teleport to the Grand Exchange.");
                    }
                } else {
                    System.out.println("(teleportToGE) -- Grand Exchange option not found in the menu.");
                }
            } else {
                System.out.println("(teleportToGE) -- Failed to interact with the Ornate Jewellery Box.");
            }
        } else {
            System.out.println("(teleportToGE) -- Ornate Jewellery Box not found or not in view.");
        }
    }
    public static void teleportToCorp() {
        System.out.println("(teleportToCorp) -- Teleporting to Corp");
        GameObject jewelleryBox = Objects.stream().name("Ornate Jewellery Box").first();
        // Check if the object is found and in viewport.
        if (jewelleryBox.valid() && jewelleryBox.inViewport()) {
            System.out.println("(teleportToCorp) -- Interacting with ornate jewellery box...");
            // Interact with the Ornate Jewellery Box.
            if (jewelleryBox.interact("Teleport Menu")) {
                // Wait for the teleport menu to load.
                Condition.wait(() -> Components.stream().widget(590).text("Corporeal Beast").viewable().isNotEmpty(), 1000, 10);
                // Now interact with the Grand Exchange option.
                Component corpBeastEntranceComponent = Components.stream().widget(590).text("Corporeal Beast").viewable().first();
                if (corpBeastEntranceComponent.valid()) {
                    corpBeastEntranceComponent.click();
                    // Wait until the player is at the Grand Exchange
                    boolean arrivedAtCorpEntrance = Condition.wait(() -> {
                        Location currentLocation = getCurrentLocation();
                        return currentLocation == Location.CORPOREAL_BEAST_ENTRANCE;
                    }, 1000, 10);

                    if (arrivedAtCorpEntrance) {
                        System.out.println("(teleportToCorp) -- Successfully teleported to the Corporeal Beast Entrance.");
                    } else {
                        System.out.println("(teleportToCorp) -- Failed to teleport to the Corporeal Beast Entrance.");
                    }
                } else {
                    System.out.println("(teleportToCorp) -- Corporeal Beast option not found in the menu.");
                }
            } else {
                System.out.println("(teleportToCorp) -- Failed to interact with the Ornate Jewellery Box.");
            }
        } else {
            System.out.println("(teleportToCorp) -- Ornate Jewellery Box not found or not in view.");
        }
    }
    public static void enterCorporealBeastRoom() {
        int corpBeastEntrance = 677;
        GameObject entrance = Objects.stream().id(corpBeastEntrance).findFirst().orElse(null);
        if (entrance != null) {
            if (!Prayer.prayersActive()) {
                activatePrayer(Prayer.Effect.PROTECT_FROM_MAGIC);
                activatePrayer(Prayer.Effect.PIETY);
            }
            if (entrance.interact("Go-through")) {
                Condition.wait(() -> getCurrentLocation() == Location.CORPOREAL_BEAST_ROOM, 250, 20);
            }
        }
    }
    public static void updatePhaseVariables() {
        // For pb devs:  This method just updates how many hits/damage done when weakening corp
        if (newExp > currentExp) {
            if (Equipment.stream().name(spec1_MAIN_HAND).findFirst().orElse(null) != null) {
                dragonHammerHits ++;
            } else if (Equipment.stream().name(spec2_MAIN_HAND).findFirst().orElse(null) != null) {
                arclightHits ++;
            } else if (Equipment.stream().name(spec3_MAIN_HAND).findFirst().orElse(null) != null) {
                double damageDone = (newExp - currentExp) / 6.33;
                bandosDamage += (int) Math.floor(damageDone);
            }
        }
        System.out.println("dragonHammer hits = " + dragonHammerHits);
        System.out.println("arclight hits = " + arclightHits);
        System.out.println("bandos damage = " + bandosDamage);
    }
    public static Phase getCurrentPhase() {
        // For pb devs:  This method handles which phase of weakening/killing corp we are in
        if (dragonHammerHits < 3) {
            System.out.println("(getCurrentPhase) -- Phase 1");
            return Phase.Phase1;
        } else if (arclightHits < 20) {
            System.out.println("(getCurrentPhase) -- Phase 2");
            return Phase.Phase2;
        } else if (bandosDamage < 200) {
            System.out.println("(getCurrentPhase) -- Phase 3");
            return Phase.Phase3;
        } else {
            System.out.println("(getCurrentPhase) -- Phase 4");
            return Phase.Phase4;
        }
    }
    public static boolean missingFullGear() {
        // For pb devs:  This one checks for all items on first start up / after a corp kill
        System.out.println("(missingFullGear) -- started this script.");
        Map<String, Integer> startingSupplies = new HashMap<>();
        startingSupplies.put("Prayer potion(4)", 2);
        startingSupplies.put("Divine super combat potion(4)", 2);
        startingSupplies.put("Anglerfish", 13);

        for (String itemName : requiredItems) {
            if (Inventory.stream().name(itemName).findFirst().orElse(null) == null
                    && Equipment.stream().name(itemName).findFirst().orElse(null) == null) {
                System.out.println("(missingFullGear) -- Missing gear:  " + itemName);
                return true;
            }
        }

        for (Map.Entry<String, Integer> entry : startingSupplies.entrySet()) {
            if (Inventory.stream().name(entry.getKey()).count() < entry.getValue()) {
                System.out.println("(missingFullGear) -- Missing items");
                return true;
            }
        }
        corpAlive = true;
        System.out.println("(missingFullGear) -- Have all gear, setting Corp Beast to Alive");
        return false;
    }
    public static int getTotalSips(String basePotionName) {
        // For pb devs:  This feeds information to missingItems()
        System.out.println("(getTotalSips) -- started this script.");
        int totalSips = 0;
        for (int i = 1; i <= 4; i++) {
            totalSips += (int) (Inventory.stream().name(basePotionName + "(" + i + ")").count() * i);
        }
        System.out.println("(getTotalSips) -- Total Sips:  " + totalSips);
        return totalSips;
    }
    public static boolean missingItems() {
        // For pb devs:  This one is checked each time the POH is visited after beginning a kill - checks for supplies, but not everything
        System.out.println("(missingItems) -- started this script.");
        Map<String, Integer> requiredSips = new HashMap<>();
        requiredSips.put("Prayer potion", 1); // 2 potions * 4 sips each
        requiredSips.put("Divine super combat potion", 1); // 2 potions * 4 sips each
        requiredSips.put("Anglerfish", 4);
        for (Map.Entry<String, Integer> entry : requiredSips.entrySet()) {
            // For potions, check total sips
            if (entry.getKey().contains("potion")) {
                if (getTotalSips(entry.getKey()) < entry.getValue()) {
                    System.out.println("(missingItems) -- Missing potions");
                    return true;
                }
            } else { // For non-potions, just check the count
                if (Inventory.stream().name(entry.getKey()).count() < entry.getValue()) {
                    System.out.println("(missingItems) -- Missing food");
                    return true;
                }
            }
        }
        return false;
    }
    public static void retrieveMissingItemsFromBank() {
        Tile bankGE = new Tile(3166, 3489, 0);
        Map<String, Integer> requiredSupplies = new HashMap<>();
        requiredSupplies.put("Prayer potion(4)", 2);
        requiredSupplies.put("Divine super combat potion(4)", 2);
        requiredSupplies.put("Anglerfish", 13);
        if (!Bank.inViewport()) {
            Movement.moveTo(bankGE);
            System.out.println("(retrieveMissingItemsFromBank) -- Moving to Bank until in Viewport");
            Condition.wait(Bank::inViewport, 500, 10);
        }
        System.out.println("(retrieveMissingItemsFromBank) -- Opening Bank");
        if (!Bank.opened()) {
            Condition.wait(Bank::open, 1000, 15);
        }
        if (!Bank.opened()) {
            System.out.println("(retrieveMissingItemsFromBank) -- Failed to open bank!");
            return;
        }
        Bank.currentTab(9);
        // deposit all items that are not part of the required items and gear
        List<String> allRequired = new ArrayList<>(requiredItems);
        allRequired.addAll(requiredSupplies.keySet());
        System.out.println("(retrieveMissingItemsFromBank) -- Depositing all items not part of required items");
        Bank.depositAllExcept(allRequired.toArray(new String[0]));
        // retrieve gear items if they're missing from inventory and equipment
        for (String item : requiredItems) {
            if (Inventory.stream().name(item).findFirst().orElse(null) == null &&
                    Equipment.stream().name(item).findFirst().orElse(null) == null) {
                System.out.println("(retrieveMissingItemsFromBank) -- Withdrawing gear required");
                Bank.withdraw(item, 1);
            }
        }
        // retrieve supplies if they're missing or less than required
        for (Map.Entry<String, Integer> entry : requiredSupplies.entrySet()) {
            long currentAmount = Inventory.stream().name(entry.getKey()).count();
            if (currentAmount < entry.getValue()) {
                System.out.println("(retrieveMissingItemsFromBank) -- Withdrawing items required");
                Bank.withdraw(entry.getKey(), (int) (entry.getValue() - currentAmount));
            }
        }
        System.out.println("(retrieveMissingItemsFromBank) -- Closing bank");
        Bank.close();
    }
    public static boolean areItemsEquipped(List<String> items) {
        // For pb devs:  This one feeds information to checkEquippedGear()
        System.out.println("(areItemsEquipped) -- started this script.");
        return items.stream().allMatch(itemName -> {
            for (Equipment.Slot slot : Equipment.Slot.values()) {
                Item item = Equipment.itemAt(slot);
                if (item.name().equals(itemName)) {
                    System.out.println("(areItemsEquipped) -- " + item.name() + " is equipped");
                    return true;
                }
                System.out.println("(areItemsEquipped) -- " + item.name() + " is NOT equipped");
            }
            return false;
        });
    }
    public static GearPhase checkEquippedGear() {
        // For pb devs:  Handles which phase of gear we are in
        // Ensure the equipment tab is open
        if (!Equipment.INSTANCE.opened()) {
            if (!Equipment.INSTANCE.open()) {
                return GearPhase.Unknown;  // Unable to open equipment, so can't determine phase
            }
            // Wait for equipment tab to be opened
            Condition.wait(Equipment.INSTANCE::opened, 300, 10);
        }
        // Define the items required for each phase
        List<String> phaseOneItems = Arrays.asList(spec1_HEAD, spec1_CAPE, spec1_MAIN_HAND, OFF_HAND,
                spec1_RING, NECK, QUIVER, TORSO, LEGS, HANDS, FEET);
        List<String> phaseTwoItems = Arrays.asList(spec2_HEAD, spec1_CAPE, spec2_MAIN_HAND, OFF_HAND,
                spec2_RING, NECK, QUIVER, TORSO, LEGS, HANDS, FEET);
        List<String> phaseThreeItems = Arrays.asList(spec3_HEAD, spec3_CAPE, spec3_MAIN_HAND,
                spec3_RING, NECK, QUIVER, TORSO, LEGS, HANDS, FEET);
        List<String> phaseFourItems = Arrays.asList(spec3_HEAD, spec3_CAPE, spec4_MAIN_HAND,
                spec3_RING, NECK, QUIVER, TORSO, LEGS, HANDS, FEET);
        if (areItemsEquipped(phaseOneItems)) {
            System.out.println("(checkEquippedGear) -- PhaseOneGear is equipped");
            return GearPhase.PhaseOneGear;
        }
        if (areItemsEquipped(phaseTwoItems)) {
            System.out.println("(checkEquippedGear) -- PhaseTwoGear is equipped");
            return GearPhase.PhaseTwoGear;
        }
        if (areItemsEquipped(phaseThreeItems)) {
            System.out.println("(checkEquippedGear) -- PhaseThreeGear is equipped");
            return GearPhase.PhaseThreeGear;
        }
        if (areItemsEquipped(phaseFourItems)) {
            System.out.println("(checkEquippedGear) -- PhaseFourGear is equipped");
            return GearPhase.PhaseFourGear;
        }
        System.out.println("(checkEquippedGear) -- Phase of Gear isn't known");
        return GearPhase.Unknown;
    }
    public static void equipGear(String phase, Map<String, Pair<Equipment.Slot, String>> items) {
        // For pb devs:  Equips correct phase of gear
        System.out.println("(" + phase + ") -- Equipping " + phase);
        for (Map.Entry<String, Pair<org.powbot.api.rt4.Equipment.Slot, String>> entry : items.entrySet()) {
            final Item ITEM = Inventory.stream().name(entry.getKey()).findFirst().orElse(null);
            if (ITEM != null) {
                String action = entry.getValue().getSecond(); // This will be "Wield", "Wear", or "Equip"
                ITEM.interact(action);
                Condition.wait(() -> {
                    Item equippedItem = Equipment.itemAt(entry.getValue().getFirst());
                    return equippedItem.valid() && equippedItem.id() == ITEM.id();
                }, 150, 33);
            }
        }
    }
    public static void gearSetup(String phase) {
        // For pb devs:  Sets the gear required for each phase, without redundant code
        Map<String, String> mainHandMap = Map.of(
                "firstPhaseGear", spec1_MAIN_HAND,
                "secondPhaseGear", spec2_MAIN_HAND,
                "thirdPhaseGear", spec3_MAIN_HAND,
                "fourthPhaseGear", spec4_MAIN_HAND
        );
        Map<String, String> headMap = Map.of(
                "firstPhaseGear", spec1_HEAD,
                "secondPhaseGear", spec2_HEAD,
                "thirdPhaseGear", spec3_HEAD,
                "fourthPhaseGear", spec3_HEAD
        );
        Map<String, String> ringMap = Map.of(
                "firstPhaseGear", spec1_RING,
                "secondPhaseGear", spec2_RING,
                "thirdPhaseGear", spec3_RING,
                "fourthPhaseGear", spec3_RING
        );
        Map<String, String> capeMap = Map.of(
                "firstPhaseGear", spec1_CAPE,
                "secondPhaseGear", spec1_CAPE,
                "thirdPhaseGear", spec3_CAPE,
                "fourthPhaseGear", spec3_CAPE
        );
        if (mainHandMap.containsKey(phase)) {
            Map<String, Pair<Equipment.Slot, String>> items = generateGearMap(
                    mainHandMap.get(phase),
                    headMap.get(phase),
                    ringMap.get(phase),
                    capeMap.get(phase)
            );
            equipGear(phase, items);
        } else {
            System.out.println("Invalid phase provided: " + phase);
        }
    }
    private static Map<String, Pair<Equipment.Slot, String>> generateGearMap(String mainHand, String head, String ring, String cape) {
        // For pb devs:  Simplifies interactions with items
        Map<String, Pair<Equipment.Slot, String>> items = new HashMap<>();
        items.put(mainHand, new Pair<>(Equipment.Slot.MAIN_HAND, "Wield"));
        items.put(OFF_HAND, new Pair<>(Equipment.Slot.OFF_HAND, "Wield"));
        items.put(head, new Pair<>(Equipment.Slot.HEAD, "Wear"));
        items.put(ring, new Pair<>(Equipment.Slot.RING, "Wear"));
        items.put(cape, new Pair<>(Equipment.Slot.CAPE, "Wear"));
        items.put(NECK, new Pair<>(Equipment.Slot.NECK, "Wear"));
        items.put(TORSO, new Pair<>(Equipment.Slot.TORSO, "Wear"));
        items.put(LEGS, new Pair<>(Equipment.Slot.LEGS, "Wear"));
        items.put(HANDS, new Pair<>(Equipment.Slot.HANDS, "Wear"));
        items.put(FEET, new Pair<>(Equipment.Slot.FEET, "Wear"));
        items.put(QUIVER, new Pair<>(Equipment.Slot.QUIVER, "Equip"));
        return items;
    }
    public static boolean needCombatPotion() {
        System.out.println("(needCombatPotion) -- Checking for combat potion");
        int boostedAttackLevel = Skills.level(Skill.Attack);
        int realAttackLevel = Skills.realLevel(Skill.Attack);
        return boostedAttackLevel <= realAttackLevel;
    }
    public static void consumeItem(String itemName, int[] itemIds, String action, String message) {
        // For pb devs:  Instead of 3 different methods for prayer potions, food, and combat potions....
        System.out.println("(" + message + ") -- " + message);
        Item item = (itemIds != null) ? Inventory.stream().id(itemIds).findFirst().orElse(null) : Inventory.stream().name(itemName).findFirst().orElse(null);
        Location location = getCurrentLocation();
        if (item != null) {
            item.interact(action);
            System.out.println(action + " " + itemName);
        } else {
            if (location != Location.POH && location != Location.GRAND_EXCHANGE) {
                System.out.println("(" + message + ") -- Items depleted, teleporting to POH.");
                teleportToPOH();
            }
        }
    }
    public static void grabItemFromGround(String itemName) {
        // For pb devs:  Loot grabber when corp dies
        GroundItem groundItem = GroundItems.stream().within(15).name(itemName).nearest().first();
        if (groundItem.inViewport()) {
            int invCount = (int) GroundItems.stream().id(groundItem.id()).at(groundItem.tile()).count();
            String action = itemName.equals(corpPet) ? "Pick-up" : "Take";
            groundItem.interact(action, groundItem.name());
            Condition.wait(() -> GroundItems.stream().id(groundItem.id()).at(groundItem.tile()).count() < invCount, 50, 150);
        }
    }
    public static void drinkPool() {
        System.out.println("Drinking from pool...");
        Objects.stream().within(10).id(29241).findFirst().ifPresent(ornatePool -> ornatePool.interact("Drink"));
        Condition.wait(() -> Combat.health() == Combat.maxHealth(), 500, 20);
    }
    public static void attackCorporealBeast(){
        String addName = "Dark energy core";
        DEC = Npcs.stream().within(2).name(addName).firstOrNull();
        String bossName = "Corporeal Beast";
        CB = Npcs.stream().within(25).name(bossName).firstOrNull();
        int currentHP = Combat.healthPercent();
        int currentPrayerPoints = Prayer.prayerPoints();
        int specialAttackPercent = Combat.specialPercentage();
        Phase phase = getCurrentPhase();
        System.out.println("currentHP (healthPercent) = " + currentHP);
        System.out.println("specialAttackPercent = " + specialAttackPercent);
        if (!corpAlive) {
            grabItemFromGround(corpPet);
            grabItemFromGround(Arrays.toString(wantedItems));
            teleportToPOH();
        } else {
            if (CB != null) {
                if (currentHP < 50 || ((phase == Phase.Phase1 || phase == Phase.Phase2 || phase == Phase.Phase3) &&
                        specialAttackPercent < 50)) {
                    teleportToPOH();
                } else if (currentHP < 77 && phase == Phase.Phase4) {
                    consumeItem("Anglerfish", null, "Eat", "eatFood");
                } else if (currentPrayerPoints < 50 && phase == Phase.Phase4) {
                    consumeItem("Prayer potion", new int[]{2434, 139, 141, 143}, "Drink", "drinkPrayerPotion");
                } else if (needCombatPotion()) {
                    consumeItem("Divine combat potion", new int[]{28527, 28525, 28523, 28521}, "Drink", "drinkCombatPotion");
                } else {
                    if (DEC != null) {
                        System.out.println("(attackCorporealBeast) -- DEC is alive and not null");
                        if (Combat.specialAttack(false)) {
                            if (DEC.inViewport()) {
                                DEC.interactionType(ModelInteractionType.HullAccurate).interact("Attack", addName);
                                Condition.wait(() -> Players.local().interacting().equals(DEC), 150, 10);
                            } else {
                                int angleToBoss = Camera.angleToLocatable(CB.tile());
                                int angle90degrees = angleToBoss + 90;
                                Camera.turnTo(angle90degrees,Camera.pitch());
                            }
                        }
                    } else {
                        if (CB.inViewport()) {
                            if (phase == Phase.Phase1 || phase == Phase.Phase2 || phase == Phase.Phase3) {
                                Combat.specialAttack(true);
                            }
                            if (!Players.local().interacting().equals(CB)) {
                                CB.interactionType(ModelInteractionType.HullAccurate).interact("Attack", bossName);
                                Condition.wait(() -> Players.local().interacting().equals(CB), 500, 10);
                            }
                        } else {
                            Camera.angleToLocatable(CB.tile());
                        }
                    }
                }
            } else {
                int floor = Players.local().floor();
                Tile playerTile = Players.local().tile();
                Tile moveTile = new Tile(playerTile.x() + 10, playerTile.y(), floor);
                Movement.step(moveTile);
            }
        }
    }
    @Override
    public void onStart() {
        chatSubscriber chatSub = new chatSubscriber();
        Events.register(chatSub);
    }
    public static class chatSubscriber {
        @Subscribe
        public void onMessage(MessageEvent event) {
            if (event.getMessage().contains("retrieved some of your items")) {
                StopScript();
            }
            if (event.getMessage().contains("Corporeal Beast kill count")) {
                corpAlive = false;
                bossKills ++;
                System.out.println("Corp Kill Count: " + bossKills);
            }
        }
    }
    public static void activatePrayer(Prayer.Effect prayerEffect) {
        // For pb devs:  Instead of correctly handling a simple prayer multiple times, make it one line
        if (!Prayer.prayerActive(prayerEffect)) {
            prayer(prayerEffect, true);
            Condition.wait(() -> Prayer.prayerActive(prayerEffect), 100, 10);
        } else {
            return;
        }
        System.out.println("Praying " + prayerEffect.name());
    }
    @Override
    public void poll() {
        Location location = getCurrentLocation();
        Phase phase = getCurrentPhase();
        GearPhase currentGear = checkEquippedGear();
        switch (location) {
            case POH:
                System.out.println("Player is in the player owned house - check if corp alive...");
                if (!corpAlive) {
                    System.out.println("Fresh boss, verifying full gear and supplies...");
                    boolean gearCheckCompleted = Condition.wait(() -> !missingFullGear(), 500, 10);
                    if (!gearCheckCompleted) {
                        System.out.println("Missing gear and supplies, going to grand exchange...");
                        teleportToGE();
                        return;
                    }
                } else {
                    System.out.println("Not fresh boss, verifying items...");
                    boolean itemCheckCompleted = Condition.wait(() -> !missingItems(), 500, 10);
                    if (!itemCheckCompleted) {
                        System.out.println("Missing supplies, going to bank...");
                        teleportToGE();
                        return;
                    }
                }
                if (phase == Phase.Phase1) {
                    System.out.println("Phase 1, require 3 Dragon hammer hits.");
                    if (currentGear != GearPhase.PhaseOneGear) {
                        System.out.println("Not wearing Phase One Gear, equipping...");
                        gearSetup("firstPhaseGear");
                        return;
                    }
                } else if (phase == Phase.Phase2) {
                    System.out.println("Phase 2, require 20 Arclight hits.");
                    if (currentGear != GearPhase.PhaseTwoGear) {
                        System.out.println("Not wearing Phase Two Gear, equipping...");
                        gearSetup("secondPhaseGear");
                        return;
                    }
                } else if (phase == Phase.Phase3) {
                    System.out.println("Phase 3, require 200 Bandos godsword damage.");
                    if (currentGear != GearPhase.PhaseThreeGear) {
                        System.out.println("Not wearing Phase Three Gear, equipping...");
                        gearSetup("thirdPhaseGear");
                        return;
                    }
                } else if (phase == Phase.Phase4) {
                    System.out.println("Phase 4, kill Corporeal Beast.");
                    if (currentGear != GearPhase.PhaseFourGear) {
                        System.out.println("Not wearing Phase Four Gear, equipping...");
                        gearSetup("fourthPhaseGear");
                        return;
                    }
                } else {
                    System.out.println("Unknown phase");
                    teleportToPOH(); //remove when using stop script
                    // TODO: StopScript();
                }
                if (needCombatPotion()) {
                    System.out.println("Missing combat potion buff, drinking potion...");
                    if (Combat.health() > 10) {
                        consumeItem("Divine combat potion", new int[]{28527, 28525, 28523, 28521}, "Drink", "drinkCombatPotion");
                    } else {
                        drinkPool();
                    }
                    return;
                }
                drinkPool();
                System.out.println("Going to corp...");
                teleportToCorp();
                break;
            case CORPOREAL_BEAST_ENTRANCE:
                    System.out.println("At Corporeal Beast Entrance - entering Corp's Room...");
                enterCorporealBeastRoom();
                break;
            case CORPOREAL_BEAST_ROOM:
                    System.out.println("In Corporeal Beast Room - finding Corp Beast...");
                currentExp = Skills.experience(Skill.Attack);
                attackCorporealBeast();
                newExp = Skills.experience(Skill.Attack);
                    System.out.println("currentExp Attack = " + currentExp);
                    System.out.println("newExp Attack = " + newExp);
                updatePhaseVariables();
                break;
            case GRAND_EXCHANGE:
                if (!corpAlive) {  // check for full amount of gear / supplies for starting
                        System.out.println("At GE, verifying gear/supplies...");
                    if (missingFullGear()) {
                        System.out.println("Missing items, getting gear/supplies...");
                        retrieveMissingItemsFromBank();
                    } else {
                        teleportToPOH();
                    }
                } else {
                    if (missingItems()) {
                        System.out.println("Missing items, getting gear/supplies...");
                        retrieveMissingItemsFromBank();
                    } else {
                        teleportToPOH();
                    }
                }
                return;
            case UNKNOWN:
                System.out.println("Unknown location, please either start in POH, GE, or Corp's Lair");
                StopScript();
                break;
        }
    }
    public static void StopScript() {
        // For pb devs:  I am stumped here.  This shit just crashes the entire client instead of simply stopping the runtime.
        System.out.println("(StopScript) -- Stopping the script...");
        if (Game.loggedIn()) {
            System.out.println("(StopScript) -- logging out");
            Game.logout();
            System.out.println("(StopScript) -- exit script");
            System.exit(0);
        }
    }
    public static void main(String[] args) {
        new parasCorpBeast().startScript();
    }
}
