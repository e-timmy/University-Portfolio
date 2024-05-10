--Implement your solution here
--SEE THE PROJECT CODING GUIDELINES ON THE LMS FOR DETAILS OF
--THE CRITERIA THAT WILL BE EMPLOYED IN ASSESSING YOUR CODE.
--Please DELETE THIS WHOLE COMMENT, and write your own.

module Proj2 (Pitch, toPitch, feedback,
            GameState, initialGuess, nextGuess) where

-- Libraries
import Data.Char
import Data.List
import Data.Ord

-------------------------------------------------------------------------

-- toPitch Functionality

data Pitch = Pitch Note Oct 
    deriving Eq
instance Show Pitch where
    show (Pitch n o) = (show n) ++ (show o) 

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

data Oct = One | Two | Three
    deriving Eq
instance Show Oct where
    show One = "1"
    show Two = "2"
    show Three = "3"

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
 
-- Converts Char to Note 
toNote :: Char -> Note
toNote c 
    | c == 'A' = A
    | c == 'B' = B
    | c == 'C' = C
    | c == 'D' = D
    | c == 'E' = E
    | c == 'F' = F
    | c == 'G' = G
    | otherwise = error("Incorrect Note. Use one of ['A', 'B', 'C', 'D', 'E', 'F', 'G']")

-- Converts Char to Octave
toOct :: Char ->  Oct
toOct x
    | x == '1' = One
    | x == '2' = Two
    | x == '3' = Three
    | otherwise = error("Incorrect Octave. Use one of ['1', '2', '3']")

pitchToString :: Pitch -> String
pitchToString (Pitch n o) = [noteToChar n] ++ [octToChar o]

noteToChar :: Note -> Char
noteToChar note
    | note == A = 'A'
    | note == B = 'B'
    | note == C = 'C'
    | note == D = 'D'
    | note == E = 'E'
    | note == F = 'F'
    | note == G = 'G'
    | otherwise = error("Note not given correctly.")

octToChar :: Oct -> Char
octToChar oct
    | oct == One = '1'
    | oct == Two = '2'
    | oct == Three = '3'
    | otherwise = error("Octave not given correctly.")

noteTypes = [A, B, C, D, E, F, G]
octaveTypes = [One, Two, Three]
chordLength = 3

-- Checks to see if Note will be valid transformation
validNote :: Char -> Bool
validNote c = c `elem` ['A', 'B', 'C', 'D', 'E', 'F', 'G']

-- Checks to see if Oct will be valid transformation
validOctave :: Char -> Bool
validOctave x
    | x `elem` ['1', '2', '3'] = True
    | otherwise = False

-- Returns note from pitch
pitchNote :: Pitch -> Note
pitchNote (Pitch c _) = c

-- Returns octave from pitch
pitchOct :: Pitch -> Oct
pitchOct (Pitch _ o) = o
    
-------------------------------------------------------------------------
-- feedback functionality

-- Takes a target and a gess, returns feedback in triple consisting of
--     Correct Pitches
--     Correct Notes but wrong octave
--     Correct Octaves but wrong note
-- Sample tests:
-- feedback [Pitch A One, Pitch B Two, Pitch A Three] [ Pitch A One, Pitch A Two, Pitch B One]
-- feedback [Pitch A One, Pitch B Two, Pitch C Three] [ Pitch A One, Pitch A Two, Pitch A Three]
-- feedback [Pitch A One, Pitch B One, Pitch C One] [Pitch A Two, Pitch D One, Pitch E One]
-- feedback [Pitch A Three, Pitch B Two, Pitch C One] [Pitch C Three, Pitch A Two, Pitch B One]
-- Failed for: [A1, A2, E3] and guess [F1, G1, G2]
-- feedback [Pitch A One, Pitch A Two, Pitch E Three] [Pitch F One, Pitch G One, Pitch G Two]
feedback :: [Pitch] -> [Pitch] -> (Int, Int, Int)
feedback target guess =
    let matches = pitchMatches target guess
        t_remain = chordElements (target \\ matches)
        g_remain = chordElements (guess \\ matches)
    in (length matches, 0, 0) `sumTrips` chordSimilarity t_remain g_remain

-- Given matches removed counts similarities
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
  
-- Returns matching pitches
-- Test: pitchMatches [Pitch A One, Pitch B Two, Pitch C Three] [Pitch A One, Pitch B Two, Pitch C Three]
pitchMatches :: [Pitch] -> [Pitch] -> [Pitch]
pitchMatches ts gs = [t | t <- ts, g <- gs, t == g]

-- Gets unique elements of chord
-- Test: chordElements [Pitch A One, Pitch B Two, Pitch C Three]
chordElements :: [Pitch] -> String
chordElements ps = concat ([pitchToString p| p <- ps])

-- Sums triples
sumTrips :: Num a => (a, a, a) -> (a, a, a) -> (a, a, a)
sumTrips (x1, y1, z1) (x2, y2, z2) = (x1+x2, y1+y2, z1+z2)

-------------------------------------------------------------------------

-- Guess Functionality

-- List of guesses, list of feedback, list of remaining possibilities
type GameState = ([[Pitch]], [(Int, Int, Int)],  [[Pitch]])

-- Takes no input, returns pair of an initial guess and a game state
initialGuess :: ([Pitch], GameState)
initialGuess = 
    let firstGuess = [Pitch E One, Pitch E Two, Pitch E Three]
        state = ([], [], deleteChord firstGuess allChords)
    in (firstGuess, state)

-- Takes as input pair of previous guess and game state and feedback
-- Returns pair of next guess and game state
nextGuess :: ([Pitch], GameState) -> (Int, Int, Int) -> ([Pitch], GameState)
nextGuess ([], _) _ = error("Out of guesses")
nextGuess (lastGuess, state@(guesses, feed, possibles)) fback =
    let newGuesses = guesses ++ [lastGuess]
        newFeed = feed ++ [fback]
        --newPossibles = delete lastGuess possibles
        newPossibles = removeImpossibles (newGuesses, newFeed, possibles)
        newState = (newGuesses, newFeed, newPossibles)
        -- chord = head newPossibles
        chord = bestPossible newPossibles
    in (chord, newState)

-- Returns pitch and amount of recurrent feedback
-- Max first: ([G1,G2,G3],540)
-- Min first: ([A1,B1,C2],196)
bestPossible :: [[Pitch]] -> [Pitch]
bestPossible ps = 
    fst (maximumBy (comparing snd) [(p, mostFeedback p ps)| p <- ps])

-- Returns number of recurring feedback for given pitch
-- Test function
mostFeedback :: [Pitch] -> [[Pitch]] -> Int
mostFeedback t (p:ps) =
   maximum (map length (group (sort ([feedback t p| p <- ps]))))

-- removes impossibles given new game state
removeImpossibles :: GameState -> [[Pitch]]
removeImpossibles (_,_,[]) = []
removeImpossibles (gs, fs, (p:ps)) 
   | possible = p : removeImpossibles (gs, fs, ps)
   | otherwise = removeImpossibles (gs, fs, ps)
   where possible = possibleChord gs fs p

-- Assumes feedback must be same for each guess given
possibleChord :: [[Pitch]] -> [(Int, Int, Int)] -> [Pitch] -> Bool
possibleChord [] [] _ = True
possibleChord gs fs p = fs == [feedback g p |g <- gs]

-- Generates all combinations of pitches
allPitches :: [Pitch]
allPitches = [(Pitch n o)| n <- noteTypes, o <- octaveTypes]

-- Generates all combinations of chords given pitches
allChords :: [[Pitch]]
allChords = combinations chordLength allPitches

-- Need new delete functions
deleteChord :: [Pitch] -> [[Pitch]] -> [[Pitch]]
deleteChord chord (c:cs)
    | chord `chordMatch` c = cs
    | otherwise = c : deleteChord chord cs

-- Gets all combinations of list for given length
combinations :: Int -> [a] -> [[a]]
combinations 0 _ = [[]]
combinations _ [] = []
combinations n (x:xs) = (map (x:) (combinations (n-1) xs)) ++ (combinations n xs)

-- 

-------------------------------------------------------------------------
-- Extras 

-- Generates all combinations of chords given pitches
allChordsBeta :: [[Pitch]]
allChordsBeta = 
    let ps = allPitches
    in [[p1, p2, p3]| p1 <- ps, p2 <- ps, p3 <-ps, p1 /= p2, p1 /= p3, 
        p2 /= p3]
        
-- Checks if chord an element of a list of chords
-- Tests:
-- chordElem [Pitch A One, Pitch B Two, Pitch C Three] [[Pitch C Three, Pitch B Two, Pitch A One]]
chordElem :: [Pitch] -> [[Pitch]] -> Bool
chordElem _ [] = False
chordElem chord (c:cs)
    | chord `chordMatch` c = True
    | otherwise = chordElem chord cs

-- Equates chords
-- Tests:
-- chordMatch [Pitch A One, Pitch B Two, Pitch C Three] [Pitch C Three, Pitch B Two, Pitch A One]
chordMatch :: [Pitch] -> [Pitch] -> Bool
chordMatch [] [] = True
chordMatch _ [] = False
chordMatch [] _ = False
chordMatch target (g:gs) 
    | g `elem` target = chordMatch new_ts gs
    | otherwise = False
    where new_ts = delete g target
    
-------------------------------------------------------------------------

-- Todo: Add in information ->