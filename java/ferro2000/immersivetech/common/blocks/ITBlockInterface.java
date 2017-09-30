package ferro2000.immersivetech.common.blocks;

import net.minecraft.util.EnumFacing;

public class ITBlockInterface {
	
	public interface IMechanicalEnergy{
		
		boolean isMechanicalEnergyTransmitter();
		
		boolean isMechanicalEnergyReceiver();
		
		EnumFacing getMechanicalEnergyOutputFacing();
		
		EnumFacing getMechanicalEnergyInputFacing();
		
		int inputToCenterDistance();
		
	}

}
