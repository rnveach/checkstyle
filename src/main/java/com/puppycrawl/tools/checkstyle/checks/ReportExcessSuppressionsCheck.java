///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
///////////////////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks;

import java.util.TreeSet;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.Definitions;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Filter;
import com.puppycrawl.tools.checkstyle.api.MessageDispatcher;
import com.puppycrawl.tools.checkstyle.api.Violation;
import com.puppycrawl.tools.checkstyle.filters.SuppressFilterElement;
import com.puppycrawl.tools.checkstyle.filters.SuppressionFilter;

/**
 * Report excess suppressions.
 */
public class ReportExcessSuppressionsCheck extends AutomaticBean implements AuditListener {
    @Override
    public void auditStarted(AuditEvent event) {
        // no code by default
    }

    @Override
    public void auditFinished(AuditEvent event) {
        if (event.getSource() instanceof Checker) {
            final MessageDispatcher dispatcher = (Checker) event.getSource();

            for (Filter filter1 : ((Checker) event.getSource()).getFilters()) {
                if (filter1 instanceof SuppressionFilter) {
                    final String path = ((SuppressionFilter) filter1).getFile();
                    final TreeSet<Violation> errors = new TreeSet<>();

                    dispatcher.fireFileStarted(path);

                    for (Filter filter2 : ((SuppressionFilter) filter1).getFilters()) {
                        if (filter2 instanceof SuppressFilterElement) {
                            final SuppressFilterElement element = (SuppressFilterElement) filter2;

                            if (!element.isUsed()) {
                                errors.add(new Violation(-1, Definitions.CHECKSTYLE_BUNDLE,
                                        "suppression.unused", new String[] {
                                            element.toString(),
                                        }, null, getClass(), null));
                            }
                        }
                    }

                    dispatcher.fireErrors(path, errors);
                    dispatcher.fireFileFinished(path);
                }
            }
        }
    }

    @Override
    public void fileStarted(AuditEvent event) {
        // no code by default
    }

    @Override
    public void fileFinished(AuditEvent event) {
        // no code by default
    }

    @Override
    public void addError(AuditEvent event) {
        // no code by default
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {
        // no code by default
    }

    @Override
    protected void finishLocalSetup() throws CheckstyleException {
        // no code by default
    }

}
