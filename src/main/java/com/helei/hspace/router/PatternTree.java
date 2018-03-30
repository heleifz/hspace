package com.helei.hspace.router;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * pattern matching based on tree traversal
 */
public class PatternTree
{
    private Node root = new RootNode();
    /**
     * pattern priority, the lower the better 
     */
    private HashMap<String, Integer> patternPriority = new HashMap<>();

    public void addPattern(String pattern, String tag) {
        List<Token> tokens = Token.tokenize(pattern);
        updateTree(tokens, tag);
        return;
    }

    public MatchResult match(String query) {
        List<Token> tokens = Token.tokenize(query);
        SearchState initial = SearchState.create(tokens);
        Node root = getRoot();
        List<MatchResult> allMatches = root.match(initial);
        if (allMatches.isEmpty()) {
            return null;
        }
        MatchResult bestMatch = selectBest(allMatches);
        return bestMatch;
    }

    private MatchResult selectBest(List<MatchResult> matches) {
        return Collections.min(matches, (a, b)->{
            String tagA = a.getTag();
            String tagB = b.getTag();
            return patternPriority.get(tagA).compareTo(patternPriority.get(tagB));
        });
    }

    private Node getRoot() {
        return root;
    }

    private void updateTree(List<Token> tokens, String tag) {
        int score = patternScore(tokens);
        patternPriority.put(tag, score);
        Node currentNode = root;
        for (int i = 0; i < tokens.size(); ++i) {
            Token tok = tokens.get(i);
            List<Node> nexts = currentNode.getChildren();
            boolean isCompatible = false;
            Node compatibleNode = null;
            for (Node n : nexts) {
                if (n.isCompatible(tok)) {
                    isCompatible = true;
                    compatibleNode = n;
                    break; 
                }
            }
            if (isCompatible) {
                // simply change current node
                currentNode = compatibleNode;
            } else {
                // create new node
                String t = null;
                if (i + 1 == tokens.size()) {
                    t = tag;
                }
                Node newNode = Node.createFromToken(tok, t);
                currentNode.addChild(newNode);
                currentNode = newNode;
            }

        }
    }

    private int patternScore(List<Token> tokens) {
        int score = 0;
        for (Token t : tokens) {
            Token.Type type = t.getType();
            if (type == Token.Type.WILDCARD) {
                score += 2;
            } else if (type == Token.Type.CAPTURE) {
                score += 1; 
            } else if (type == Token.Type.METHOD) {
                score -= 1; 
            }
        } 
        return score;
    }

    private static class SearchState  {
        public List<Token> tokens;
        public int position = 0;
        public HashMap<String, String> captures;

        public static SearchState create(List<Token> tokens) {
            SearchState newState = new SearchState(); 
            newState.tokens = tokens;
            newState.position = 0;
            newState.captures = new HashMap<String, String>();
            return newState; 
        }
        
        public SearchState move(int newPosition) {
            SearchState newState = new SearchState(); 
            newState.tokens = tokens;
            newState.position = newPosition;
            newState.captures = (HashMap<String,String>)captures.clone();
            return newState;
        }
        
        public SearchState copy() {
            SearchState newState = new SearchState(); 
            newState.tokens = tokens;
            newState.position = position;
            newState.captures = (HashMap<String,String>)captures.clone();
            return newState;
        }
    };

    private static abstract class Node {

        private String tag = null;
        private List<Node> children = new ArrayList<>();

        public static Node createFromToken(Token token, String tag) {
            Token.Type type = token.getType();
            String text = token.getText();
            if (type == Token.Type.TEXT) {
                return new TextNode(tag, text); 
            } else if (type == Token.Type.METHOD) {
                return new MethodNode(tag, text); 
            } else if (type == Token.Type.CAPTURE) {
                return new CaptureNode(tag, text); 
            } else if (type == Token.Type.WILDCARD) {
                return new WildcardNode(tag); 
            }
            throw new IllegalArgumentException("Illegal node type");
        }

        public Node(String tag) {
            this.tag = tag;
        }

        public List<MatchResult> match(SearchState state) {
            if (state.position >= state.tokens.size()) {
                return Collections.emptyList();
            }
            // candidate next states (for wildcard)
            List<SearchState> possible = matchThis(state);
            if (possible.isEmpty()) {
                return Collections.emptyList();
            }
            ArrayList<MatchResult> results = new ArrayList<>();
            // this node is a matched node
            String currentTag = getTag();
            if (currentTag != null) {
                results.add(new MatchResult(currentTag, 
                    (HashMap<String, String>)state.captures.clone())); 
            }
            for (SearchState s : possible) {
                for (Node n : getChildren()) {
                    results.addAll(n.match(s.copy()));
                }
            }
            return results;
        }

        public void addChild(Node node) {
            children.add(node);
        }

        public abstract boolean isCompatible(Token token);

        /**
         *  match and get next state(s)
         */
        public abstract List<SearchState> matchThis(SearchState state);
        /**
         *  get result tag if current node is terminate node
         */
        public String getTag() {
            return tag; 
        }
        /**
         *  get children of current node
         */
         public List<Node> getChildren() {
            return children;
        }
    };
    
    private static class RootNode extends Node {
        public RootNode() {
            super(null); 
        }
        @Override
        public List<SearchState> matchThis(SearchState state) {
            return Collections.singletonList(state);
        }

		@Override
		public boolean isCompatible(Token token) {
			return true;
		}
    };
    
    private static class TextNode extends Node {

        private String text;

        public TextNode(String tag, String text) {
            super(tag); 
            this.text = text;
        }
        @Override
        public List<SearchState> matchThis(SearchState state) {
            if (state.tokens.get(state.position).getText().equals(text)) {
                return Collections.singletonList(state.move(state.position + 1));
            } else {
                return Collections.emptyList(); 
            }
        }

		@Override
		public boolean isCompatible(Token token) {
            return (token.getType() == Token.Type.TEXT &&
                    token.getText().equals(text));
		}
    };

    private static class MethodNode extends Node {

        private HashSet<String> methods = new HashSet<>();

        public MethodNode(String tag, String methods) {
            super(tag); 
            for (String m : methods.split(",")) {
                this.methods.add(m);
            }
        }

        @Override
        public List<SearchState> matchThis(SearchState state) {
            Token tok = state.tokens.get(state.position);
            int pos = state.position;
            if (tok.getType() != Token.Type.METHOD) {
                // NOTE: proceed without changing position
                return Collections.singletonList(state.move(pos));
            } else if (pos == 0) {
                String[] parts = tok.getText().split(",");
                for (String p : parts) {
                    if (methods.contains(p)) {
                        return Collections.singletonList(state.move(pos + 1));
                    }
                }
                return Collections.emptyList(); 
            } else {
                return Collections.emptyList(); 
            }
            
        }

		@Override
		public boolean isCompatible(Token token) {
			if (token.getType() != Token.Type.METHOD) {
                return false; 
            }
            String[] parts = token.getText().split(",");
            if (parts.length != methods.size()) {
                return false;
            }
            for (String p : parts) {
                if (!methods.contains(p)) {
                    return false;
                }
            }
            return true;
		}
    };
    
    private static class CaptureNode extends Node {

        private String captureName;

        public CaptureNode(String tag, String captureName) {
            super(tag); 
            this.captureName = captureName;
        }
        
        @Override
        public List<SearchState> matchThis(SearchState state) {
            Token tok = state.tokens.get(state.position);
            int pos = state.position;
            if (tok.getType() == Token.Type.TEXT) {
                state.captures.put(captureName, tok.getText());
                SearchState newState = state.move(pos + 1);
                return Collections.singletonList(newState);
            } else {
                return Collections.emptyList(); 
            }
        }

		@Override
		public boolean isCompatible(Token token) {
            return (token.getType() == Token.Type.CAPTURE &&
                    token.getText().equals(captureName));
		}
    };

    private static class WildcardNode extends Node {

        public WildcardNode(String tag) {
            super(tag); 
        }
        
        @Override
        public List<SearchState> matchThis(SearchState state) {
            int pos = state.position;
            // generate a new search state for every matched position
            ArrayList<SearchState> newStates = new ArrayList<>();
            for (int i = pos; i < state.tokens.size(); ++i) {
                newStates.add(state.move(i + 1));
            }
            return newStates;
        }

		@Override
		public boolean isCompatible(Token token) {
            return (token.getType() == Token.Type.WILDCARD);
		}
    };


}
