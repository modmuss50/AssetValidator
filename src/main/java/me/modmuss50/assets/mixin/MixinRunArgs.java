package me.modmuss50.assets.mixin;

import me.modmuss50.assets.AssetValidator;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RunArgs.class)
public class MixinRunArgs {

	@Inject(method = "<init>", at = @At("RETURN"))
	public void construct(RunArgs.Network network, WindowSettings windowSettings, RunArgs.Directories dirs, RunArgs.Game game, RunArgs.AutoConnect autoConnect, CallbackInfo info) {
		try {
			AssetValidator.validate(dirs.assetDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
