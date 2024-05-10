------------------------------------------------------------------------------
-- Filename: Proj2.hs
-- Author:   Timothy Holland <tholland1@student.unimelb.edu.au> 697004
-- Purpose:  Implements and solves Musician puzzle game
-- Musician Puzzle Description:
--     - Two players: Composer and Performer.
--     - Game begins with Composer selecting target.
--           - Target: three-pitch musical chord.
--           - Pitch comprises:
--                 - one musical note (A,B,C,D,E,F,G).
--                 - one octave (1,2,3).
--     - Next, Performer guesses chord.
--     - Until guess matches target:
--           - Composer provides feedback on guess.
--           - Feedback comprises triple:
--                 - Correct pitches: how many pitches included in target.
--                 - Correct notes: how many pitches with right note but 
--                       wrong octave.
--                 - Correct octaves: how many pitches with right octave
--                       but wrong note.
--           - Note: in counting correct cords/octaves, multiple occurences
--                 in guess only counted as correct if repeated in target.
--     - Game finishes when guess matches target.
--     - Goal: minimise number of guesses taken to reach target.
-- Implementation Description: The first four sections of code provide 
--     functionality to support a solver:
--     - S1: Data type and show for Pitch/Note/Octave.
--           Note and Octave defined as non-string equivalents.
--           Pitch defined as one note followed by one octave.
--     - S2: Conversions from String to Pitch (for user input).
--           Checks valid length (2). Checks each char valid note/octave. 
--           Constructs note/octave from each. Constructs Pitch.
--     - S3: Chord data structure manipulation funtions. 
--     - S4: Feedback for each guess/target pair. First, matches are counted.
--           Remainder of guess and target reduced to Strings. 
--           Iterates over guess elements, removes from target if also
--           an element. Constructs triple from counts of each type of match.
-- Solver Description: Last section provides solution for game.
--     - S5: Guess implementation. 
--           Initial guess optimised through (non exhaustive) testing over all
--           combinations of chords, to find lowest average guess.
--           Next guess optimised through minimising solution space.
--           An all chord quess space generated. Based on feedback, impossible
--           guesses removed.
--           Each remaining prospective guess tested. Feedback generated on it
--           as target, and remaining solutions as guesses. Based on future 
--           feedback, remaining solutions grouped.
--           Guess chosen that minimises average size of its feedback groups.
-- Main.hs provides driver for game, which calls the essential game functions.
-- Tested on all chords, averages 4.26 guesses per target.
------------------------------------------------------------------------------

module Proj2 (Pitch, toPitch, feedback, GameState, 
    initialGuess, nextGuess) where

-- Libraries
import Data.Char
import Data.List
import Data.Maybe
import Data.Ord

------------------------------------------------------------------------------

-- * Section 1: Pitch/Note/Octave Construction.
--   Data constructors for pitch, note, and octave.

-- | Pitch is one note followed by one ocatave.
data Pitch = Pitch Note Oct 
    deriving Eq
instance Show Pitch where
    show (Pitch n o) = (show n) ++ (show o) 

-- | Note is given letter
--   To add note, must also edit section 2.
data Note = A | B | C | D | E | F | G
    deriving Eq
instance Show Note where
    show A = "A"
    show B = "B"
    show C = "C"
    show D = "D"
    show E = "E"
    show F = "F"
    show G = "G"

-- | Octave is given number.
--   To add octave, must also edit section 2.
data Oct = One | Two | Three
    deriving Eq
instance Show Oct where
    show One = "1"
    show Two = "2"
    show Three = "3"

-- | Scope for Notes and Octaves.
noteTypes = [A, B, C, D, E, F, G]
octaveTypes = [One, Two, Three]


------------------------------------------------------------------------------

-- * Section 2: Pitch/Note/Octave Functionality.
--   Conversion functions for pitches, notes, octaves.

-- * 2.1 Pitch Functionality.

-- | Converts valid string to pitch.
--   Otherwise returns Nothing.
--   Validates by checking valid note followed by valid ocatave.
toPitch :: String -> Maybe Pitch
toPitch pitch@(n:o:err) 
    | len == 2 && valid_note && valid_oct = Just (Pitch note oct)
    | otherwise = Nothing
    where len = length pitch
          valid_note = validNote n
          valid_oct = validOctave o
          note = toNote n
          oct = toOct o
toPitch _ = Nothing 

-- | Convert Pitch to String.
pitchToString :: Pitch -> String
pitchToString (Pitch n o) = noteToChar n ++ octToChar o


-- * 2.2 Note Funtionality.

-- | Validate character to note transformation.
validNote :: Char -> Bool
validNote c = c `elem` ['A', 'B', 'C', 'D', 'E', 'F', 'G']

-- | Convert valid char to note.
toNote :: Char -> Note
toNote c 
    | c == 'A' = A
    | c == 'B' = B
    | c == 'C' = C
    | c == 'D' = D
    | c == 'E' = E
    | c == 'F' = F
    | c == 'G' = G
    | otherwise = error("Use one of ['A', 'B', 'C', 'D', 'E', 'F', 'G']")

-- | Convert Note to Character.
noteToChar :: Note -> String
noteToChar note
    -- | note == A = 'A'
    -- | note == B = 'B'
    -- | note == C = 'C'
    -- | note == D = 'D'
    -- | note == E = 'E'
    -- | note == F = 'F'
    -- | note == G = 'G'
    -- | otherwise = error("Note not given correctly.")
    = show note 

-- | Return note from pitch.
pitchNote :: Pitch -> Note
pitchNote (Pitch c _) = c


-- * 2.3 Octave Functionality.

-- | Validate character to octave transformation.
validOctave :: Char -> Bool
validOctave x
    | x `elem` ['1', '2', '3'] = True
    | otherwise = False

-- | Convert Char to Octave.
toOct :: Char ->  Oct
toOct x
    | x == '1' = One
    | x == '2' = Two
    | x == '3' = Three
    | otherwise = error("Incorrect Octave. Use one of ['1', '2', '3']")

-- | Convert Octave to Character.
octToChar :: Oct -> String
octToChar oct
    -- | oct == One = '1'
    -- | oct == Two = '2'
    -- | oct == Three = '3'
    -- | otherwise = error("Octave not given correctly.")
    = show oct

-- | Return octave from pitch.
pitchOct :: Pitch -> Oct
pitchOct (Pitch _ o) = o


------------------------------------------------------------------------------

-- * Section 3: Chord Functionality.
--  Manipulation of chords and data structures containing chords.

-- | Length of chord.
chordLength = 3

-- | Generate all combinations of pitches.
allPitches :: [Pitch]
allPitches = [(Pitch n o)| n <- noteTypes, o <- octaveTypes]

-- | Generate all combinations of chords given pitches.
allChords :: [[Pitch]]
allChords = uniqueCombos chordLength allPitches

-- | Parse a string containing a number of space-separated pitches to 
--   produce a list of pitches. Error if any of the pitches can't be parsed.
toChord :: String -> [Pitch]
toChord = (fromJust . mapM toPitch . words)

-- | Convert chord to String.
chordToString :: [Pitch] -> String
chordToString ps = concat ([pitchToString p| p <- ps])

-- | Unique combinations of size n for given list.
--   First argument is length of combinations, second is list.
uniqueCombos :: Int -> [a] -> [[a]]
uniqueCombos 0 _ = [[]]
uniqueCombos _ [] = []
uniqueCombos n (x:xs) =
    let xCombos = map (x:) (uniqueCombos (n-1) xs)
        rest = uniqueCombos n xs
    in xCombos ++ rest

-- | Delete chord from list of chords.
deleteChord :: [Pitch] -> [[Pitch]] -> [[Pitch]]
deleteChord chord (c:cs)
    | chord `chordMatch` c = cs
    | otherwise = c : deleteChord chord cs

-- | Check if two chords match.
chordMatch :: [Pitch] -> [Pitch] -> Bool
chordMatch [] [] = True
chordMatch _ [] = False
chordMatch [] _ = False
chordMatch target (g:gs) 
    | g `elem` target = chordMatch new_ts gs
    | otherwise = False
    where new_ts = delete g target


------------------------------------------------------------------------------
-- * Section 3: Feedback functionality
--   Provides feedback for musician's chord guess.

-- | Give feedback on chord guess given target. 
--   Returns triple containing:
--        Correct pitches
--        Correct notes but wrong octaves
--        Correct ocatves but wrong notes
--   First argument is target chord, second is guess.
feedback :: [Pitch] -> [Pitch] -> (Int, Int, Int)
feedback target guess =
    let matches = pitchMatches target guess -- Find matches
        t_remain = chordToString (target \\ matches) -- Target remainder
        g_remain = chordToString (guess \\ matches) -- Guess remainder
    in (length matches, 0, 0) `sumTrips` chordSimilarity t_remain g_remain

-- | Find matching pitches in two chords
pitchMatches :: [Pitch] -> [Pitch] -> [Pitch]
pitchMatches ts gs = [t | t <- ts, g <- gs, t == g]

-- | Check note and octave matches for feedback.
--   Iterates over guess. When element of target, remove from target.
--   Assumes matches parsed out of target and guess using pitchMatches.
--   First argument is target, second is guess.
chordSimilarity :: String -> String -> (Int, Int, Int)
chordSimilarity ts [] = (0,0,0)
chordSimilarity ts (g:gs)
    | match && note = (0, 1, 0) `sumTrips` chordSimilarity new_ts gs
    | match && oct = (0, 0, 1) `sumTrips` chordSimilarity new_ts gs
    | note = chordSimilarity ts gs
    | oct = chordSimilarity ts gs
    | otherwise = error("Element not note nor octave.")
    where match = g `elem` ts
          note = validNote g
          oct = validOctave g
          new_ts = delete g ts
  
-- | Sumation of two triples.
sumTrips :: Num a => (a, a, a) -> (a, a, a) -> (a, a, a)
sumTrips (x1, y1, z1) (x2, y2, z2) = (x1+x2, y1+y2, z1+z2)

------------------------------------------------------------------------------

-- ** Section 4: Guess Functionality
--    Logic behind Performer's chord guess
--    Iterates over remaining possible guesses.
--    Using feedback function, posits possible future feedbacks given guess.
--    Chooses guess with lowest average size of feedback groups.

-- | Define GameState as triple of:
--       List of past guesses
--       List of past feedback
--       List of remaining possible guesses
type GameState = ([[Pitch]], [(Int, Int, Int)],  [[Pitch]])

-- | Make first guess and initialise GameState.
--   First guess optimised by minimising average guesses over allChords.
initialGuess :: ([Pitch], GameState)
initialGuess = 
    let firstGuess = [Pitch A One, Pitch B Two, Pitch C One]
        state = ([], [], deleteChord firstGuess allChords)
    in (firstGuess, state)

-- | Make guess given state of game. Using feedback:
--       Remove impossible guesses remaining.
--       Find optimal given optimising functoin (averageFeedback)
--   Takes tuple of past guess and new GameState, followed by triple
--   of feedback.
--   Returns optimal guess and updated gamestate.
nextGuess :: ([Pitch], GameState) -> (Int, Int, Int) -> ([Pitch], GameState)
nextGuess (_, (_,_,[])) _ = error("Out of guesses")
nextGuess (lastGuess, state@(guesses, feed, possibles)) fback =
    let newGuesses = guesses ++ [lastGuess]
        newFeed = feed ++ [fback]
        newPossibles = removeImpossibleChords (newGuesses, newFeed, possibles)
        newState = (newGuesses, newFeed, newPossibles)
        chord = bestGuess newPossibles
    in (chord, newState)

-- | Remove impossible guesses given new game state. 
--   Takes GameState as argument. Returns possible chords.
removeImpossibleChords :: GameState -> [[Pitch]]
removeImpossibleChords (_,_,[]) = []
removeImpossibleChords (gs, fs, (p:ps)) 
   | possible = p : removeImpossibleChords (gs, fs, ps)
   | otherwise = removeImpossibleChords (gs, fs, ps)
   where possible = possibleChord gs fs p

-- | Check whether chord possible given past guesses and feedbacks.
--   Assumes feedback must be same for each guess given.
--   First argument is past guess, second is past feedbacks,
--   Third argument is chord to be checked.
possibleChord :: [[Pitch]] -> [(Int, Int, Int)] -> [Pitch] -> Bool
possibleChord [] [] _ = True
possibleChord gs fs p = fs == [feedback g p |g <- gs]

-- | Optimal guess by minimising average remaining possible targets
--   Takes list of possible chords. Returns chord.
bestGuess :: [[Pitch]] -> [Pitch]
bestGuess ps = 
    fst (minimumBy (comparing snd) [(p, averageFeedback p ps)| p <- ps])

-- | Find average size of chord's possible feedback groups, by:
--       Generate feedback for each possible given target.
--       Sort list. Group feedbacks. Count size of each group.
--       Average.
--   First argument is potential target, second is possible chords.
averageFeedback :: [Pitch] -> [[Pitch]] -> Int
averageFeedback t (p:ps) =
    let feedGroups = map length (group (sort (([feedback t p| p <- ps]))))
        fgLen = length feedGroups
    in (sum feedGroups) `div` fgLen   

------------------------------------------------------------------------------