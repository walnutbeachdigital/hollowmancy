Hollowmancy boss models - Blockbench projects

Open any .bbmodel here in Blockbench (File > Open Model). The texture is built in.

To change a model:
 1. Edit it in Blockbench (keep the part names - the animations look them up by name).
 2. File > Export > Export Java Entity, with "Mojang Mappings" for 1.21.
 3. In the exported file, copy only the body of createBodyLayer(...).
 4. Paste it into src/main/java/com/walnutbeach/hollowmancy/client/model/<Boss>Model.java,
    replacing the body of createBodyLayer(float grow). Keep the animation code (setupAnim) as it is.
 5. File > Save Texture over src/main/resources/assets/hollowmancy/textures/entity/<boss>.png if you repainted it.

Glowing parts are in <boss>_glow.png (same layout; transparent = doesn't glow).
