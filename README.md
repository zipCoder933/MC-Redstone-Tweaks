# MC-Redstone-Tweaks (v1)
Made to be directly compatable with redstone pen: https://github.com/stfwi/redstonepen
The only changes that this mod makes are the removal of the RCI (Redstone Client Interface) and asthetic improvements (New textures, and removal of dedicated creative mode tab)

## Performance notes
* Tile entities
  * The only tile entities in this mod are the Control box and redstone track
* Redstone relay
  * The tick event in the redstone relay is only called when scheduled
    * It changes its state when the redstone power has changed
    * The update event schedules a tick when the block or its neighbors are updated