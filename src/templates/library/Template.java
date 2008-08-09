package templates.library;

import ides.api.model.fsa.FSAModel;

import javax.swing.Icon;

public interface Template
{
	public String getName();

	public Icon getIcon();

	public FSAModel instantiate();
}
