# Disable Vanilla Tutorial Hints

Vanilla tutorial text and some triggers are data-driven, but the step flow is hardcoded client code under `net.minecraft.client.tutorial`.

Useful data points:

- `assets/minecraft/lang/en_us.json` can override visible tutorial text, such as `tutorial.find_tree.description`.
- `data/minecraft/tags/block/completes_find_tree_tutorial.json` changes which looked-at blocks complete the find-tree step.
- `data/minecraft/tags/item/completes_find_tree_tutorial.json` changes which collected items complete the find-tree step.
- `PunchTreeTutorialStepInstance` still hardcodes `BlockTags.LOGS` for progress and completion.

When mod progression no longer matches vanilla's tree/planks tutorial, disable the vanilla tutorial client-side once on `ClientStartedEvent` instead of trying to partially redirect it:

```java
if (minecraft.options.tutorialStep != TutorialSteps.NONE) {
    minecraft.options.tutorialStep = TutorialSteps.NONE;
    minecraft.options.save();
}
```

Register this only on the physical client. Keep gameplay authority in common/server events.
