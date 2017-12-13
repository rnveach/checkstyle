////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
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
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.utils;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Class used to track and identify frames and which methods/variables are in said frame.
 * @author Richard Veach
 */
public class FrameTrackingUtil {
    /** Tree of all the parsed frames. */
    private final Map<DetailAST, AbstractFrame> frames = new HashMap<>();

    public Map<DetailAST, AbstractFrame> getFrames() {
        return frames;
    }

    /**
     * Used to reset the frames of the entire class.
     * @param rootAST The new root to reset based on.
     */
    public void reset(DetailAST rootAST) {
        frames.clear();

        final Deque<AbstractFrame> frameStack = new LinkedList<>();
        DetailAST curNode = rootAST;
        while (curNode != null) {
            collectDeclarations(frameStack, curNode);
            DetailAST toVisit = curNode.getFirstChild();
            while (curNode != null && toVisit == null) {
                endCollectingDeclarations(frameStack, curNode);
                toVisit = curNode.getNextSibling();
                if (toVisit == null) {
                    curNode = curNode.getParent();
                }
            }
            curNode = toVisit;
        }
    }

    /**
     * Parses the next AST for declarations.
     * @param frameStack stack containing the FrameTree being built.
     * @param ast AST to parse.
     */
    private static void collectDeclarations(Deque<AbstractFrame> frameStack, DetailAST ast) {
        final AbstractFrame frame = frameStack.peek();
        switch (ast.getType()) {
            case TokenTypes.VARIABLE_DEF :
                collectVariableDeclarations(ast, frame);
                break;
            case TokenTypes.PARAMETER_DEF :
                if (!CheckUtils.isReceiverParameter(ast)
                        && ast.getParent().getType() != TokenTypes.LITERAL_CATCH) {
                    final DetailAST parameterIdent = ast.findFirstToken(TokenTypes.IDENT);
                    frame.addIdent(parameterIdent);
                }
                break;
            case TokenTypes.CLASS_DEF :
            case TokenTypes.INTERFACE_DEF :
            case TokenTypes.ENUM_DEF :
            case TokenTypes.ANNOTATION_DEF :
                final DetailAST classFrameNameIdent = ast.findFirstToken(TokenTypes.IDENT);
                frameStack.addFirst(new ClassFrame(frame, classFrameNameIdent));
                break;
            case TokenTypes.SLIST :
                frameStack.addFirst(new BlockFrame(frame, ast));
                break;
            case TokenTypes.METHOD_DEF :
                final DetailAST methodFrameNameIdent = ast.findFirstToken(TokenTypes.IDENT);
                final DetailAST mods = ast.findFirstToken(TokenTypes.MODIFIERS);
                if (mods.findFirstToken(TokenTypes.LITERAL_STATIC) == null) {
                    ((ClassFrame) frame).addInstanceMethod(methodFrameNameIdent);
                }
                else {
                    ((ClassFrame) frame).addStaticMethod(methodFrameNameIdent);
                }
                frameStack.addFirst(new MethodFrame(frame, methodFrameNameIdent));
                break;
            case TokenTypes.CTOR_DEF :
                final DetailAST ctorFrameNameIdent = ast.findFirstToken(TokenTypes.IDENT);
                frameStack.addFirst(new ConstructorFrame(frame, ctorFrameNameIdent));
                break;
            case TokenTypes.ENUM_CONSTANT_DEF :
                final DetailAST ident = ast.findFirstToken(TokenTypes.IDENT);
                ((ClassFrame) frame).addStaticMember(ident);
                break;
            case TokenTypes.LITERAL_CATCH:
                final AbstractFrame catchFrame = new CatchFrame(frame, ast);
                catchFrame.addIdent(ast.findFirstToken(TokenTypes.PARAMETER_DEF).findFirstToken(
                        TokenTypes.IDENT));
                frameStack.addFirst(catchFrame);
                break;
            case TokenTypes.LITERAL_FOR:
                final AbstractFrame forFrame = new ForFrame(frame, ast);
                frameStack.addFirst(forFrame);
                break;
            case TokenTypes.LITERAL_NEW:
                if (isAnonymousClassDef(ast)) {
                    frameStack.addFirst(new AnonymousClassFrame(frame,
                            ast.getFirstChild().toString()));
                }
                break;
            case TokenTypes.LAMBDA:
                final AbstractFrame lambdaFrame = new LambdaFrame(frame, ast);
                final DetailAST parameter = ast.findFirstToken(TokenTypes.IDENT);

                if (parameter != null) {
                    lambdaFrame.addIdent(parameter);
                }

                frameStack.addFirst(lambdaFrame);
                break;
            default:
                // do nothing
        }
    }

    /**
     * Collects variable declarations.
     * @param ast variable token.
     * @param frame current frame.
     */
    private static void collectVariableDeclarations(DetailAST ast, AbstractFrame frame) {
        final DetailAST ident = ast.findFirstToken(TokenTypes.IDENT);
        if (frame.getType() == FrameType.CLASS_FRAME) {
            final DetailAST mods =
                    ast.findFirstToken(TokenTypes.MODIFIERS);
            if (ScopeUtils.isInInterfaceBlock(ast)
                    || mods.findFirstToken(TokenTypes.LITERAL_STATIC) != null) {
                ((ClassFrame) frame).addStaticMember(ident);
            }
            else {
                ((ClassFrame) frame).addInstanceMember(ident);
            }
        }
        else {
            frame.addIdent(ident);
        }
    }

    /**
     * Ends parsing of the AST for declarations.
     * @param frameStack Stack containing the FrameTree being built.
     * @param ast AST that was parsed.
     */
    private void endCollectingDeclarations(Queue<AbstractFrame> frameStack, DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.CLASS_DEF :
            case TokenTypes.INTERFACE_DEF :
            case TokenTypes.ENUM_DEF :
            case TokenTypes.ANNOTATION_DEF :
            case TokenTypes.SLIST :
            case TokenTypes.METHOD_DEF :
            case TokenTypes.CTOR_DEF :
            case TokenTypes.LITERAL_CATCH :
            case TokenTypes.LITERAL_FOR :
            case TokenTypes.LAMBDA:
                frames.put(ast, frameStack.poll());
                break;
            case TokenTypes.LITERAL_NEW :
                if (isAnonymousClassDef(ast)) {
                    frames.put(ast, frameStack.poll());
                }
                break;
            default :
                // do nothing
        }
    }

    /**
     * Whether the AST is a definition of an anonymous class.
     * @param ast the AST to process.
     * @return true if the AST is a definition of an anonymous class.
     */
    private static boolean isAnonymousClassDef(DetailAST ast) {
        final DetailAST lastChild = ast.getLastChild();
        return lastChild != null
            && lastChild.getType() == TokenTypes.OBJBLOCK;
    }

    /**
     * Retrieves the frame belonging to the given {@code ast}.
     * @param ast The AST to examine.
     * @return The frame belonging to the AST if there is one.
     */
    public AbstractFrame getFrame(DetailAST ast) {
        return frames.get(ast);
    }

    /** An AbstractFrame type. */
    public enum FrameType {

        /** Class frame type. */
        CLASS_FRAME,
        /** Constructor frame type. */
        CTOR_FRAME,
        /** Method frame type. */
        METHOD_FRAME,
        /** Block frame type. */
        BLOCK_FRAME,
        /** Catch frame type. */
        CATCH_FRAME,
        /** Lambda frame type. */
        FOR_FRAME,
        /** Lambda frame type. */
        LAMBDA_FRAME,
        ;

        @Override
        public String toString() {
            switch (this) {
            case CLASS_FRAME:
                return "Class";
            case CTOR_FRAME:
                return "Constructor";
            case METHOD_FRAME:
                return "Method";
            case BLOCK_FRAME:
                return "Block";
            case CATCH_FRAME:
                return "Catch";
            case FOR_FRAME:
                return "For";
            case LAMBDA_FRAME:
                return "Lambda";
            default:
                return null;
            }
        }

    }

    /**
     * A declaration frame.
     * @author Stephen Bloch
     * @author Andrei Selkin
     */
    public abstract static class AbstractFrame {
        /** Set of name of variables declared in this frame. */
        private final Set<DetailAST> varIdents;

        /** Parent frame. */
        private final AbstractFrame parent;

        /** Name identifier token. */
        private final DetailAST frameNameIdent;

        /**
         * Constructor -- invocable only via super() from subclasses.
         * @param parent parent frame.
         * @param ident frame name ident.
         */
        protected AbstractFrame(AbstractFrame parent, DetailAST ident) {
            this.parent = parent;
            frameNameIdent = ident;
            varIdents = new HashSet<>();
        }

        /**
         * Get the type of the frame.
         * @return a FrameType.
         */
        public abstract FrameType getType();

        /**
         * Add a name to the frame.
         * @param identToAdd the name we're adding.
         */
        private void addIdent(DetailAST identToAdd) {
            varIdents.add(identToAdd);
        }

        public Set<DetailAST> getVarIdents() {
            return varIdents;
        }

        public AbstractFrame getParent() {
            return parent;
        }

        public String getFrameName() {
            return frameNameIdent.getText();
        }

        public DetailAST getFrameNameIdent() {
            return frameNameIdent;
        }

        /**
         * Check whether the frame contains a field or a variable with the given name.
         * @param nameToFind the IDENT ast of the name we're looking for.
         * @return whether it was found.
         */
        protected boolean containsFieldOrVariable(DetailAST nameToFind) {
            return containsFieldOrVariableDef(varIdents, nameToFind);
        }

        /**
         * Check whether the frame contains a given name.
         * @param nameToFind IDENT ast of the name we're looking for.
         * @param lookForMethod whether we are looking for a method name.
         * @return whether it was found.
         */
        public AbstractFrame getIfContains(DetailAST nameToFind, boolean lookForMethod) {
            final AbstractFrame frame;

            if (!lookForMethod
                && containsFieldOrVariable(nameToFind)) {
                frame = this;
            }
            else {
                frame = parent.getIfContains(nameToFind, lookForMethod);
            }
            return frame;
        }

        /**
         * Whether the set contains a declaration with the text of the specified
         * IDENT ast and it is declared in a proper position.
         * @param set the set of declarations.
         * @param ident the specified IDENT ast.
         * @return true if the set contains a declaration with the text of the specified
         *         IDENT ast and it is declared in a proper position.
         */
        public boolean containsFieldOrVariableDef(Set<DetailAST> set, DetailAST ident) {
            boolean result = false;
            for (DetailAST ast: set) {
                if (isProperDefinition(ident, ast)) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        /**
         * Whether the definition is correspondent to the IDENT.
         * @param ident the IDENT ast to check.
         * @param ast the IDENT ast of the definition to check.
         * @return true if ast is correspondent to ident.
         */
        protected boolean isProperDefinition(DetailAST ident, DetailAST ast) {
            final String nameToFind = ident.getText();
            return nameToFind.equals(ast.getText())
                && checkPosition(ast, ident);
        }

        /**
         * Whether the declaration is located before the checked ast.
         * @param ast1 the IDENT ast of the declaration.
         * @param ast2 the IDENT ast to check.
         * @return true, if the declaration is located before the checked ast.
         */
        private static boolean checkPosition(DetailAST ast1, DetailAST ast2) {
            boolean result = false;
            if (ast1.getLineNo() < ast2.getLineNo()
                    || ast1.getLineNo() == ast2.getLineNo()
                    && ast1.getColumnNo() < ast2.getColumnNo()) {
                result = true;
            }
            return result;
        }

    }

    /**
     * A frame initiated at method definition; holds a method definition token.
     * @author Stephen Bloch
     * @author Andrei Selkin
     */
    public static class MethodFrame extends AbstractFrame {

        /**
         * Creates method frame.
         * @param parent parent frame.
         * @param ident method name identifier token.
         */
        protected MethodFrame(AbstractFrame parent, DetailAST ident) {
            super(parent, ident);
        }

        @Override
        public FrameType getType() {
            return FrameType.METHOD_FRAME;
        }

    }

    /**
     * A frame initiated at constructor definition.
     * @author Andrei Selkin
     */
    public static class ConstructorFrame extends AbstractFrame {

        /**
         * Creates a constructor frame.
         * @param parent parent frame.
         * @param ident frame name ident.
         */
        protected ConstructorFrame(AbstractFrame parent, DetailAST ident) {
            super(parent, ident);
        }

        @Override
        public FrameType getType() {
            return FrameType.CTOR_FRAME;
        }

    }

    /**
     * A frame initiated at class, enum or interface definition; holds instance variable names.
     * @author Stephen Bloch
     * @author Andrei Selkin
     */
    public static class ClassFrame extends AbstractFrame {

        /** Set of idents of instance members declared in this frame. */
        private final Set<DetailAST> instanceMembers;
        /** Set of idents of instance methods declared in this frame. */
        private final Set<DetailAST> instanceMethods;
        /** Set of idents of variables declared in this frame. */
        private final Set<DetailAST> staticMembers;
        /** Set of idents of static methods declared in this frame. */
        private final Set<DetailAST> staticMethods;

        /**
         * Creates new instance of ClassFrame.
         * @param parent parent frame.
         * @param ident frame name ident.
         */
        ClassFrame(AbstractFrame parent, DetailAST ident) {
            super(parent, ident);
            instanceMembers = new HashSet<>();
            instanceMethods = new HashSet<>();
            staticMembers = new HashSet<>();
            staticMethods = new HashSet<>();
        }

        @Override
        public FrameType getType() {
            return FrameType.CLASS_FRAME;
        }

        public Set<DetailAST> getInstanceMembers() {
            return instanceMembers;
        }

        public Set<DetailAST> getInstanceMethods() {
            return instanceMethods;
        }

        public Set<DetailAST> getStaticMembers() {
            return staticMembers;
        }

        public Set<DetailAST> getStaticMethods() {
            return staticMethods;
        }

        /**
         * Adds static member's ident.
         * @param ident an ident of static member of the class.
         */
        private void addStaticMember(final DetailAST ident) {
            staticMembers.add(ident);
        }

        /**
         * Adds static method's name.
         * @param ident an ident of static method of the class.
         */
        private void addStaticMethod(final DetailAST ident) {
            staticMethods.add(ident);
        }

        /**
         * Adds instance member's ident.
         * @param ident an ident of instance member of the class.
         */
        private void addInstanceMember(final DetailAST ident) {
            instanceMembers.add(ident);
        }

        /**
         * Adds instance method's name.
         * @param ident an ident of instance method of the class.
         */
        private void addInstanceMethod(final DetailAST ident) {
            instanceMethods.add(ident);
        }

        /**
         * Checks if a given name is a known instance member of the class.
         * @param ident the IDENT ast of the name to check.
         * @return true is the given name is a name of a known
         *         instance member of the class.
         */
        public boolean hasInstanceMember(final DetailAST ident) {
            return containsFieldOrVariableDef(instanceMembers, ident);
        }

        /**
         * Checks if a given name is a known instance method of the class.
         * @param ident the IDENT ast of the method call to check.
         * @return true if the given ast is correspondent to a known
         *         instance method of the class.
         */
        public boolean hasInstanceMethod(final DetailAST ident) {
            return containsMethodDef(instanceMethods, ident);
        }

        /**
         * Checks if a given name is a known static method of the class.
         * @param ident the IDENT ast of the method call to check.
         * @return true is the given ast is correspondent to a known
         *         instance method of the class.
         */
        public boolean hasStaticMethod(final DetailAST ident) {
            return containsMethodDef(staticMethods, ident);
        }

        /**
         * Checks whether given instance member has final modifier.
         * @param instanceMember an instance member of a class.
         * @return true if given instance member has final modifier.
         */
        public boolean hasFinalField(final DetailAST instanceMember) {
            boolean result = false;
            for (DetailAST member : instanceMembers) {
                final DetailAST mods = member.getParent().findFirstToken(TokenTypes.MODIFIERS);
                final boolean finalMod = mods.findFirstToken(TokenTypes.FINAL) != null;
                if (finalMod && member.equals(instanceMember)) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        @Override
        protected boolean containsFieldOrVariable(DetailAST nameToFind) {
            return containsFieldOrVariableDef(instanceMembers, nameToFind)
                    || containsFieldOrVariableDef(staticMembers, nameToFind);
        }

        @Override
        protected boolean isProperDefinition(DetailAST ident, DetailAST ast) {
            final String nameToFind = ident.getText();
            return nameToFind.equals(ast.getText());
        }

        @Override
        public AbstractFrame getIfContains(DetailAST nameToFind, boolean lookForMethod) {
            AbstractFrame frame = null;

            if (lookForMethod && containsMethod(nameToFind)
                || containsFieldOrVariable(nameToFind)) {
                frame = this;
            }
            else if (getParent() != null) {
                frame = getParent().getIfContains(nameToFind, lookForMethod);
            }
            return frame;
        }

        /**
         * Check whether the frame contains a given method.
         * @param methodToFind the AST of the method to find.
         * @return true, if a method with the same name and number of parameters is found.
         */
        private boolean containsMethod(DetailAST methodToFind) {
            return containsMethodDef(instanceMethods, methodToFind)
                || containsMethodDef(staticMethods, methodToFind);
        }

        /**
         * Whether the set contains a method definition with the
         *     same name and number of parameters.
         * @param set the set of definitions.
         * @param ident the specified method call IDENT ast.
         * @return true if the set contains a definition with the
         *     same name and number of parameters.
         */
        private static boolean containsMethodDef(Set<DetailAST> set, DetailAST ident) {
            boolean result = false;
            for (DetailAST ast: set) {
                if (isSimilarSignature(ident, ast)) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        /**
         * Whether the method definition has the same name and number of parameters.
         * @param ident the specified method call IDENT ast.
         * @param ast the ast of a method definition to compare with.
         * @return true if a method definition has the same name and number of parameters
         *     as the method call.
         */
        private static boolean isSimilarSignature(DetailAST ident, DetailAST ast) {
            boolean result = false;
            final DetailAST elistToken = ident.getParent().findFirstToken(TokenTypes.ELIST);
            if (elistToken != null && ident.getText().equals(ast.getText())) {
                final int paramsNumber =
                    ast.getParent().findFirstToken(TokenTypes.PARAMETERS).getChildCount();
                final int argsNumber = elistToken.getChildCount();
                result = paramsNumber == argsNumber;
            }
            return result;
        }

    }

    /**
     * An anonymous class frame; holds instance variable names.
     */
    public static class AnonymousClassFrame extends ClassFrame {

        /** The name of the frame. */
        private final String frameName;

        /**
         * Creates anonymous class frame.
         * @param parent parent frame.
         * @param frameName name of the frame.
         */
        protected AnonymousClassFrame(AbstractFrame parent, String frameName) {
            super(parent, null);
            this.frameName = frameName;
        }

        @Override
        public String getFrameName() {
            return frameName;
        }

    }

    public static class LambdaFrame extends MethodFrame {
        protected LambdaFrame(AbstractFrame parent, DetailAST ident) {
            super(parent, ident);
        }

        @Override
        public FrameType getType() {
            return FrameType.LAMBDA_FRAME;
        }
    }

    /**
     * A frame initiated on entering a statement list; holds local variable names.
     * @author Stephen Bloch
     */
    public static class BlockFrame extends AbstractFrame {

        /**
         * Creates block frame.
         * @param parent parent frame.
         * @param ident ident frame name ident.
         */
        protected BlockFrame(AbstractFrame parent, DetailAST ident) {
            super(parent, ident);
        }

        @Override
        public FrameType getType() {
            return FrameType.BLOCK_FRAME;
        }

    }

    /**
     * A frame initiated on entering a catch block; holds local catch variable names.
     * @author Richard Veach
     */
    public static class CatchFrame extends AbstractFrame {

        /**
         * Creates catch frame.
         * @param parent parent frame.
         * @param ident ident frame name ident.
         */
        protected CatchFrame(AbstractFrame parent, DetailAST ident) {
            super(parent, ident);
        }

        @Override
        public FrameType getType() {
            return FrameType.CATCH_FRAME;
        }

    }

    /**
     * A frame initiated on entering a for block; holds local for variable names.
     * @author Richard Veach
     */
    public static class ForFrame extends AbstractFrame {

        /**
         * Creates for frame.
         * @param parent parent frame.
         * @param ident ident frame name ident.
         */
        protected ForFrame(AbstractFrame parent, DetailAST ident) {
            super(parent, ident);
        }

        @Override
        public FrameType getType() {
            return FrameType.FOR_FRAME;
        }

    }

}
