package templates.utils;

import javax.swing.Icon;

public interface EntityIcon extends Icon
{
	public int BOX_DISTANCE = 20;
	public EntityIcon clone();
	public void setIsModule(boolean b);
}
