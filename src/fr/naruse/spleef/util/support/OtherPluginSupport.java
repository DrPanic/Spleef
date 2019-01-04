package fr.naruse.spleef.util.support;

public class OtherPluginSupport {
    private VaultPlugin vaultPlugin;
    public OtherPluginSupport(){
        this.vaultPlugin = new VaultPlugin();
    }

    public VaultPlugin getVaultPlugin() {
        return vaultPlugin;
    }
}
