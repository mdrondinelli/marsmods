package github.cosmicdan.sleepingoverhaul.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ClientConfig {
    private static final String sectionGeneral = "clientGeneral";
    private static final String sectionGeneralTxt = " General client-side settings.";
    public final ModConfigSpec.EnumValue<TimelapseCameraType> timelapseCameraType;
    private static final String timelapseCameraTypeTxt = " Camera effect to use under Timelapse";
    public final ModConfigSpec.IntValue timelapseDimValue;
    private static final String timelapseDimValueTxt = " Screen dim to use under Timelapse. The default value of 0 will remove the screen dim.";
    public final ModConfigSpec.BooleanValue bedRestOnEnter;
    private static final String bedRestOnEnterTxt = " If enabled, pressing enter on In Bed Chat Screen with zero chat text will also function as Sleep button.\n" +
            " Requires bedRestEnabled in server config, otherwise does nothing otherwise.";
    public final ModConfigSpec.BooleanValue inBedChatFixes;
    private static final String inBedChatFixesTxt = " [1.19.4+ only] Enhances/fixes keyboard-navigation issues on the In-Bed chat screen. Summary:\n" +
            " - Allows to re-focus the chat box by clicking on it;\n" +
            " - Prevents arrow keys from changing focus away from chat box, use Ctrl+Tab to change focus instead;\n" +
            " - Prevents ENTER from sending a chat message unless the chat box is actually focused;\n" +
            " - Allows ENTER to actually work if one of the buttons is focused instead of the chat box.\n" +
            " You will probably want to keep this enabled because of the buttons we add to the screen, but it can be disabled in case there are mod conflicts.";

    public ClientConfig(final ModConfigSpec.Builder builder) {
        builder.push(sectionGeneral).comment(sectionGeneralTxt);

        timelapseCameraType = builder
                .comment(timelapseCameraTypeTxt)
                .defineEnum("timelapseCameraType", TimelapseCameraType.SurfaceOrbit);
        timelapseDimValue = builder
                .comment(timelapseDimValueTxt)
                .defineInRange("timelapseDimValue", 0, 0, 100);
        bedRestOnEnter = builder
                .comment(bedRestOnEnterTxt)
                .define("bedRestWithChatEnter", true);
        inBedChatFixes = builder
                .comment(inBedChatFixesTxt)
                .define("inBedChatFixes", true);
        builder.pop();
    }

    public enum TimelapseCameraType {
        SurfaceOrbit,
        SurfaceRotation,
        None,
    }
}
