/*
 * This file is part of Arduino.
 *
 * Copyright 2015 Arduino LLC (http://www.arduino.cc/)
 *
 * Arduino is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, you may use this file as part of a free software
 * library without restriction.  Specifically, if other files instantiate
 * templates or use macros or inline functions from this file, or you compile
 * this file and link it with other files to produce an executable, this
 * file does not by itself cause the resulting executable to be covered by
 * the GNU General Public License.  This exception does not however
 * invalidate any other reasons why the executable file might be covered by
 * the GNU General Public License.
 */

package cc.arduino.contributions.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

@SuppressWarnings("serial")
public class FilterJTextField extends JPlaceholderTextField {

  private Timer timer;
  private int minSearchLengh = 0; // 0 == disabled min length
  
  public FilterJTextField(String hint) {
    this(hint, 500);
  }

  public FilterJTextField(String hint, int delay) {
    super(hint);
    setDisabledTextColor(Color.GRAY);

    timer = new Timer(delay, e -> {
      applyFilter();
      timer.stop();
    });

    getDocument().addDocumentListener(new DocumentListener() {
      public void removeUpdate(DocumentEvent e) {
        if(checkLength()) spawnTimer();
      }

      public void insertUpdate(DocumentEvent e) {
        if(checkLength()) spawnTimer();
      }

      public void changedUpdate(DocumentEvent e) {

      }
    });

    addActionListener(e -> {
      if (timer.isRunning()) {
        timer.stop();
      }
      applyFilter();
    });
  }

  private void spawnTimer() {
    if (timer.isRunning()) {
      timer.stop();
    }
    timer.start();
  }
  
  /**
   * Validate search length
   * @return
   */
  private boolean checkLength() {
    String filter = getText();
    return !(minSearchLengh > 0 && filter.length() !=0 && filter.length() < minSearchLengh);
  }

  public void applyFilter() {
    String filter = getText();
    
    filter = filter.toLowerCase();

    // Replace anything but 0-9, a-z, or : with a space
    filter = filter.replaceAll("[^\\x30-\\x39^\\x61-\\x7a^\\x3a]", " ");

    onFilter(filter.split(" "));
  }
  
  public void setMinSearchLengh(int minSearchLengh) {
    this.minSearchLengh = minSearchLengh;
  }
  
  public int getMinSearchLengh() {
    return minSearchLengh;
  }

  protected void onFilter(String[] strings) {
    // Empty
  }

//  private void updateStyle() {
//    if (showingHint) {
//      setText(filterHint);
//      setForeground(Color.gray);
//      setFont(getFont().deriveFont(Font.ITALIC));
//    } else {
//      setForeground(UIManager.getColor("TextField.foreground"));
//      setFont(getFont().deriveFont(Font.PLAIN));
//    }
//  }

  @Override
  public void paste() {

    // Same precondition check as JTextComponent#paste().
    if (!isEditable() || !isEnabled()) {
      return;
    }

    // Perform the paste.
    super.paste();
  }
}
