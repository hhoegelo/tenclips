package tenclips.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class TenclipsHandlerCopy extends AbstractHandler
{
	public TenclipsHandlerCopy()
	{
	}

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Clipboard.get().copy();
		return null;
	}
}
