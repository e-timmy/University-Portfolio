--  Author   : Peter Schachte
--  Purpose  : Test program
--  Copyright: (c) 2020 The University of Melbourne

-- TESTING CODE.  DO NOT EDIT.

module Main where

import Data.List
import Data.Maybe
import Proj2
import System.Random

loop2 :: [Pitch] -> [Pitch] -> GameState -> Int -> ([Pitch],Int)
loop2 target guess other guesses = 
   let answer = feedback target guess
       (guess', other') = nextGuess (guess,other) answer
   in if answer == (3, 0, 0)
       then (guess, guesses)
       else loop2 target guess' other' (guesses+1)

-- | Parse a string containing a number of space-separated pitches to produce
-- a list of pitches.  Error if any of the pitches can't be parsed.
toChord :: String -> [Pitch]
toChord = (fromJust . mapM toPitch . words)

-- | Find average guesses for different initial chords.
averageGuess :: [[Pitch]] -> [[Pitch]] -> [([Pitch], Float)]
averageGuess [] _ = []
averageGuess (s:ss) chords =
  let result = avgLoop s chords
  in result : averageGuess ss chords
  
avgLoop :: [Pitch] -> [[Pitch]] -> ([Pitch], Float)
avgLoop initial target = 
    let (guess, other) = initialGuess2 initial
        result = avgLoop2 initial target
        total = fromIntegral (sum (map snd result)) :: Float
        guessAverage = total / (fromIntegral (length target) :: Float)
    in (initial, guessAverage)

avgLoop2 :: [Pitch] -> [[Pitch]] -> [([Pitch], Int)]
avgLoop2 _ [] = []
avgLoop2 initial (t:ts) =
    let (guess, other) = initialGuess2 initial
    in (loop2 t guess other 1) : avgLoop2 initial ts

main :: IO ()
main = do
  let n = 1330
      gen = mkStdGen 10
--      chords = take n (shuffle gen allChords)
      chords = take n allChords
      averages = averageGuess initialChords chords
  putStrLn $ "Test chords: " ++ show chords
  putStrLn $ "Guess results: " ++ show averages

shuffle :: StdGen -> [a] -> [a]
shuffle g xs = shuffle' (randoms g) xs
shuffle' :: [Int] -> [a] -> [a]
shuffle' (i:is) xs = let (firsts, rest) = splitAt (i `mod` length xs) xs
                     in (head rest) : shuffle' is (firsts ++ tail rest)

initialChords = [toChord "E1 G2 F1", toChord "D2 E3 F2", toChord "C1 E3 A1", toChord "F3 A1 G3"]
-- removed: toChord "A1 A2 A3", toChord "A1 B2 A3"

-- Guess results: [([A1,B2,C3],4.338346),([A1,B1,A3],4.4157896),([A1,B1,C1],4.3789473),([A1,B2,C1],4.261654)]
-- Guess results: [([B1,C2,D3],4.330827),([B1,C1,B3],4.4345865),([B1,C1,D1],4.3789473),([B1,C2,D1],4.264662)]
-- Guess results: [([C1,D2,E1],4.2774434),([D1,E2,F1],4.2796993),([E1,F2,G1],4.2729325),([F1,G2,A1],4.26391)]