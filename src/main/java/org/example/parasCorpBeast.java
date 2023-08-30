package org.example;

import com.google.common.eventbus.Subscribe;
import org.powbot.api.*;
import org.powbot.api.event.MessageEvent;
import org.powbot.api.rt4.*;
import org.powbot.api.rt4.walking.model.Skill;
import org.powbot.api.script.*;

@ScriptConfiguration(name = "Corp Killer", description = "Kills Corporeal Beast", optionType = OptionType.BOOLEAN)
@ScriptManifest(name = "Corp Killer", description = "Kills Corporeal Beast", version = "1.0")
// TODO: finish setting up menu
public class parasCorpBeast extends AbstractScript {
    public static int bossKills = 0;
    public static boolean corpAlive = false;




    @Override
    public void onStart() {
        chatSubscriber chatSub = new chatSubscriber();
        Events.register(chatSub);
    }
    public static class chatSubscriber {
        @Subscribe
        public void onMessage(MessageEvent event) {
            if (event.getMessage().contains("retrieved some of your items")) {
                ScriptControl.StopScript();
            }
            if (event.getMessage().contains("Corporeal Beast kill count")) {
                corpAlive = false;
                bossKills ++;
                System.out.println("Corp Kill Count: " + bossKills);
            }
        }
    }
    @Override
    public void poll() {
        LocationControl.Location location = LocationControl.getCurrentLocation();
        PhaseControl.Phase phase = PhaseControl.getCurrentPhase();
        ItemControl.GearPhase currentGear = ItemControl.checkEquippedGear();

        switch (location) {
            case POH:
                System.out.println("Player is in the player owned house - check if corp alive...");
                if (!corpAlive) {
                    System.out.println("Fresh boss, verifying full gear and supplies...");
                    boolean gearCheckCompleted = Condition.wait(() -> !ItemControl.missingFullGear(), 500, 10);
                    if (!gearCheckCompleted) {
                        System.out.println("Missing gear and supplies, going to grand exchange...");
                        LocationControl.teleportToGE();
                        return;
                    }
                } else {
                    System.out.println("Not fresh boss, verifying items...");
                    boolean itemCheckCompleted = Condition.wait(() -> !ItemControl.missingItems(), 500, 10);
                    if (!itemCheckCompleted) {
                        System.out.println("Missing supplies, going to bank...");
                        LocationControl.teleportToGE();
                        return;
                    }
                }
                if (phase == PhaseControl.Phase.Phase1) {
                    System.out.println("Phase 1, require 3 Dragon hammer hits.");
                    if (currentGear != ItemControl.GearPhase.PhaseOneGear) {
                        System.out.println("Not wearing Phase One Gear, equipping...");
                        ItemControl.gearSetup("firstPhaseGear");
                        return;
                    }
                } else if (phase == PhaseControl.Phase.Phase2) {
                    System.out.println("Phase 2, require 20 Arclight hits.");
                    if (currentGear != ItemControl.GearPhase.PhaseTwoGear) {
                        System.out.println("Not wearing Phase Two Gear, equipping...");
                        ItemControl.gearSetup("secondPhaseGear");
                        return;
                    }
                } else if (phase == PhaseControl.Phase.Phase3) {
                    System.out.println("Phase 3, require 200 Bandos godsword damage.");
                    if (currentGear != ItemControl.GearPhase.PhaseThreeGear) {
                        System.out.println("Not wearing Phase Three Gear, equipping...");
                        ItemControl.gearSetup("thirdPhaseGear");
                        return;
                    }
                } else if (phase == PhaseControl.Phase.Phase4) {
                    System.out.println("Phase 4, kill Corporeal Beast.");
                    if (currentGear != ItemControl.GearPhase.PhaseFourGear) {
                        System.out.println("Not wearing Phase Four Gear, equipping...");
                        ItemControl.gearSetup("fourthPhaseGear");
                        return;
                    }
                } else {
                    System.out.println("Unknown phase");
                    LocationControl.teleportToPOH(); //remove when using stop script
                    // TODO: ScriptControl.StopScript();
                }
                if (ItemControl.needCombatPotion()) {
                    System.out.println("Missing combat potion buff, drinking potion...");
                    if (Combat.health() > 10) {
                        ItemControl.consumeItem("Divine combat potion", new int[]{28527, 28525, 28523, 28521}, "Drink", "drinkCombatPotion");
                    } else {
                        UtilityControl.drinkPool();
                    }
                    return;
                }
                UtilityControl.drinkPool();
                System.out.println("Going to corp...");
                LocationControl.teleportToCorp();
                break;
            case CORPOREAL_BEAST_ENTRANCE:
                    System.out.println("At Corporeal Beast Entrance - entering Corp's Room...");
                LocationControl.enterCorporealBeastRoom();
                break;
            case CORPOREAL_BEAST_ROOM:
                    System.out.println("In Corporeal Beast Room - finding Corp Beast...");
                PhaseControl.currentExp = Skills.experience(Skill.Attack);
                BossControl.attackCorporealBeast();
                PhaseControl.newExp = Skills.experience(Skill.Attack);
                    System.out.println("currentExp Attack = " + PhaseControl.currentExp);
                    System.out.println("newExp Attack = " + PhaseControl.newExp);
                PhaseControl.updatePhaseVariables();
                break;
            case GRAND_EXCHANGE:
                if (!corpAlive) {  // check for full amount of gear / supplies for starting
                        System.out.println("At GE, verifying gear/supplies...");
                    if (ItemControl.missingFullGear()) {
                        System.out.println("Missing items, getting gear/supplies...");
                        ItemControl.retrieveMissingItemsFromBank();
                    } else {
                        LocationControl.teleportToPOH();
                    }
                } else {
                    if (ItemControl.missingItems()) {
                        System.out.println("Missing items, getting gear/supplies...");
                        ItemControl.retrieveMissingItemsFromBank();
                    } else {
                        LocationControl.teleportToPOH();
                    }
                }
                return;
            case UNKNOWN:
                System.out.println("Unknown location, please either start in POH, GE, or Corp's Lair");
                ScriptControl.StopScript();
                break;
        }
    } // end poll()
    public static void main(String[] args) {
        // Start script
        new parasCorpBeast().startScript();
    }
}
