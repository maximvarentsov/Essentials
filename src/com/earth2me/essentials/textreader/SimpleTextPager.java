package com.earth2me.essentials.textreader;

import com.earth2me.essentials.CommandSource;
import java.util.List;

public class SimpleTextPager
{
	private final transient IText text;

	public SimpleTextPager(final IText text)
	{
		this.text = text;
	}

	public void showPage(final CommandSource sender)
	{
        text.getLines().forEach(sender::sendMessage);
	}

	public List<String> getLines()
	{
		return text.getLines();
	}

	public String getLine(int line)
	{
		if (text.getLines().size() < line)
		{
			return null;
		}
		return text.getLines().get(line);
	}
}
