package net.minusmc.scriptsplugin.injection;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minusmc.scriptsplugin.remapper.injection.transformers.AbstractJavaLinkerTransformer;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8.9")
public class ScriptsPluginTransformerLoader implements IFMLLoadingPlugin{

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {AbstractJavaLinkerTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
