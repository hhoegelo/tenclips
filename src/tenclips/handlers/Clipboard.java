package tenclips.handlers;

import java.util.ArrayList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class Clipboard
{
	private static Clipboard theClipboard = new Clipboard();
	private static int invalidStackPosition = -1;
	private Listener keyUpListener = null;
	private ArrayList<String> stack = new ArrayList<String>();
	private int stackPos = invalidStackPosition;

	private Clipboard()
	{
	}

	public static Clipboard get()
	{
		return theClipboard;
	}

	public void cut() throws BadLocationException
	{
		ISelection sel = getSelection();

		if (sel instanceof TextSelection)
		{
			copy();
			TextSelection ts = (TextSelection) sel;
			IDocument doc = getDocument();
			doc.replace(ts.getOffset(), ts.getLength(), "");
		}
	}

	public void copy()
	{
		ISelection sel = getSelection();

		if (sel instanceof TextSelection)
		{
			TextSelection ts = (TextSelection) sel;
			String currentSelection = ts.getText();

			if (currentSelection.length() > 0)
			{
				pushTextOntoStack(currentSelection);
				syncToSystemClipboard(currentSelection);
			}
		}
	}

	public void paste() throws BadLocationException
	{
		syncFromSystemClipboard();

		if (!stack.isEmpty())
		{
			ISelection sel = getSelection();

			if (sel instanceof TextSelection)
			{
				stackPos = (stackPos + 1) % stack.size();

				final TextSelection ts = (TextSelection) sel;
				final String textToPaste = insertStackTopIntoDocument(ts);
				final ITextEditor editor = getTextEditor();
				editor.selectAndReveal(ts.getOffset(), textToPaste.length());
				connectKeyUpListener(ts, textToPaste, editor);
			}
		}
	}

	private ISelection getSelection()
	{
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
	}

	private void syncToSystemClipboard(String currentSelection)
	{
		org.eclipse.swt.dnd.Clipboard cp = new org.eclipse.swt.dnd.Clipboard(Display.getCurrent());
		TextTransfer tt = TextTransfer.getInstance();
		cp.setContents(new Object[]
		{ currentSelection }, new Transfer[]
		{ tt });
	}

	private String insertStackTopIntoDocument(final TextSelection ts) throws BadLocationException
	{
		IDocument doc = getDocument();
		final String textToPaste = stack.get(stackPos);
		doc.replace(ts.getOffset(), ts.getLength(), textToPaste);
		return textToPaste;
	}

	private void removeKeyListener()
	{
		if (keyUpListener != null)
		{
			Display.getCurrent().removeFilter(SWT.KeyUp, keyUpListener);
			keyUpListener = null;
		}
	}

	private IEditorPart getEditorPart()
	{
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		return part;
	}

	private void connectKeyUpListener(final TextSelection ts, final String textToPaste, final ITextEditor editor)
	{
		removeKeyListener();

		keyUpListener = new Listener()
		{
			@Override
			public void handleEvent(Event a)
			{
				if (a.keyCode == SWT.CONTROL)
				{
					if (stackPos >= 0)
					{
						commitPaste();
						editor.selectAndReveal(ts.getOffset() + textToPaste.length(), 0);
					}
					removeKeyListener();
				}
			}
		};

		Display.getCurrent().addFilter(SWT.KeyUp, keyUpListener);
	}

	private void syncFromSystemClipboard()
	{
		org.eclipse.swt.dnd.Clipboard cp = new org.eclipse.swt.dnd.Clipboard(Display.getCurrent());
		TextTransfer transfer = TextTransfer.getInstance();
		String clipboardText = (String) cp.getContents(transfer);

		if (clipboardText != null)
		{
			pushTextOntoStack(clipboardText);
		}
	}

	private void pushTextOntoStack(String newText)
	{
		if (stack.isEmpty())
		{
			stack.add(newText);
		} else if (stack.get(0) != newText)
		{
			moveToTop(newText);
			reduceToMax();
		}
	}

	private void moveToTop(String data)
	{
		stack.remove(data);
		stack.add(0, data);
	}

	private void reduceToMax()
	{
		while (stack.size() > 10)
		{
			stack.remove(stack.size() - 1);
		}
	}

	private void commitPaste()
	{
		String s = stack.get(stackPos);
		stack.remove(stackPos);
		stack.add(0, s);
		stackPos = invalidStackPosition;
	}

	private IDocument getDocument()
	{
		ITextEditor editor = getTextEditor();
		IDocumentProvider dp = editor.getDocumentProvider();
		IDocument doc = dp.getDocument(editor.getEditorInput());
		return doc;
	}

	private ITextEditor getTextEditor()
	{
		IEditorPart part = getEditorPart();
		ITextEditor editor = (ITextEditor) part;
		return editor;
	}
}
