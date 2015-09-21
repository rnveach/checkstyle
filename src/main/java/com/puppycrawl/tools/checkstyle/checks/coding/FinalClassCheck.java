package com.puppycrawl.tools.checkstyle.checks.coding;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

public class FinalClassCheck extends Check {
	public static final String MSG_KEY = "final.class";

	private String fileName;
	private String packageName;
	private List<String> importsList = new LinkedList<String>();

	private Map<String, Frame> nonFinalClasses = new TreeMap<String, Frame>();
	private Set<String> inheritedClasses = new HashSet<String>();

	private static class Frame {
		private final String fileName;
		private final int lineNo;
		private final int columnNo;

		public Frame(String fileName, int lineNo, int columnNo) {
			this.fileName = fileName;
			this.lineNo = lineNo;
			this.columnNo = columnNo;
		}

		public String getFileName() {
			return fileName;
		}

		public int getLineNo() {
			return lineNo;
		}

		public int getColumnNo() {
			return columnNo;
		}
	}

	@Override
	public int[] getDefaultTokens() {
		return new int[] { TokenTypes.PACKAGE_DEF, TokenTypes.IMPORT, TokenTypes.CLASS_DEF };
	}

	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void beginTree(DetailAST rootAST) {
		this.fileName = getFileContents().getFileName();
		this.importsList.clear();
	}

	@Override
	public void visitToken(DetailAST ast) {
		switch (ast.getType()) {
		case TokenTypes.PACKAGE_DEF:
			this.packageName = getText(ast);
			break;
		case TokenTypes.IMPORT:
			this.importsList.add(getText(ast));
			break;
		case TokenTypes.CLASS_DEF:
			final DetailAST extend = ast.findFirstToken(TokenTypes.EXTENDS_CLAUSE);

			if (extend != null) {
				this.inheritedClasses.add(getFullClassPath(getText(extend)));
			}

			final DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);

			if ((modifiers.branchContains(TokenTypes.FINAL))
					|| (modifiers.branchContains(TokenTypes.ABSTRACT))) {
				return;
			}

			this.nonFinalClasses.put(
					getFullClassPath(getText(ast)),
					new Frame(this.fileName, ast.getLineNo(), CommonUtils.lengthExpandedTabs(
							getLines()[ast.getLineNo() - 1], ast.getColumnNo(), getTabWidth())));
			break;
		}
	}

	@Override
	public void finishProcessing() {
		for (String clss : this.nonFinalClasses.keySet()) {
			if (!this.inheritedClasses.contains(clss)) {
				final Frame frame = this.nonFinalClasses.get(clss);

				logExternal(frame.getFileName(), frame.getLineNo(), frame.getColumnNo(), MSG_KEY);
			}
		}
	}

	private String getFullClassPath(String s) {
		if (s.contains("."))
			return s;

		String find = "." + s;

		for (String imprt : this.importsList) {
			if (imprt.endsWith(find))
				return imprt;
		}

		return this.packageName + find;
	}

	/**
	 * Gets the text representation from the given DetailAST node.
	 * 
	 * @param ast
	 *            - DetailAST node is pointing to import definition or to the
	 *            "new" literal node ("IMPORT" or "LITERAL_NEW" node types).
	 * @return Import text without "import" word and semicolons for given
	 *         "IMPORT" node or instanstiated class Name&Path for given
	 *         "LITERAL_NEW" node.
	 */
	private static String getText(final DetailAST ast) {
		String result = null;

		final DetailAST textWithoutDots = ast.findFirstToken(TokenTypes.IDENT);

		if (textWithoutDots == null) {
			// if there are TokenTypes.DOT nodes in subTree.
			final DetailAST parentDotAST = ast.findFirstToken(TokenTypes.DOT);
			if (parentDotAST != null) {
				final FullIdent dottedPathIdent = FullIdent.createFullIdentBelow(parentDotAST);
				final DetailAST nameAST = parentDotAST.getLastChild();
				result = dottedPathIdent.getText() + "." + nameAST.getText();
			} else {
				ast.print();
			}
		} else { // if subtree doesn`t contain dots.
			result = textWithoutDots.getText();
		}

		return result;
	}
}
