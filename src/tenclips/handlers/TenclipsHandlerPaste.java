package tenclips.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;

public class TenclipsHandlerPaste extends AbstractHandler
{
	public TenclipsHandlerPaste()
	{
	}

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		try
		{
			Clipboard.get().paste();
		} catch (BadLocationException e)
		{
		}
		return null;
	}
}
