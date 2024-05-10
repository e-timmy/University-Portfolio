--  Author   : Peter Schachte
--  Purpose  : Test program
--  Copyright: (c) 2020 The University of Melbourne

-- TESTING CODE.  DO NOT EDIT.

module Main where

import Data.List
import Data.Maybe
import Proj2
import System.Random

-- | Guess the given target, counting and showing the guesses.
guessTest :: [Pitch] -> IO ()
guessTest target = do
      let (guess,other) = initialGuess
      loop target guess other 1

-- | Given a target and guess and a guess number, continue guessing
-- until the right target is guessed.
loop :: [Pitch] -> [Pitch] -> GameState -> Int -> IO ()
loop target guess other guesses = do
  putStrLn $ "Your guess #" ++ show guesses ++ ":  " ++ show guess
  let answer = feedback target guess
  putStrLn $ "    My answer:  " ++ show answer
  if answer == (3,0,0)
    then do
      putStrLn $ "You got it in " ++ show guesses ++ " guesses!"
    else do
      let (guess',other') = nextGuess (guess,other) answer
      loop target guess' other' (guesses+1)

loop1 :: [[Pitch]] -> [([Pitch], Int)]
loop1 [] = []
loop1 (c:cs) = 
  guessTest1 c : (loop1 cs)
  
guessTest1 :: [Pitch] -> ([Pitch], Int)
guessTest1 target =
    let (guess, other) = initialGuess
    in loop2 target guess other 1

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

-- | Prompt for a target and use guessTest to try to guess it.
main :: IO ()
main = do
  let n = 1330
      gen = mkStdGen 97
      --chords = take n (shuffle gen allChords)
      chords = take n allChords
      --chords = [toChord "E1 E2 F1"]
      guesses = loop1 chords
      total = fromIntegral (sum (map snd guesses)) :: Float
      guessAverage = total / (fromIntegral (n) :: Float)
  putStrLn $ "Generating information from " ++ show n ++ " guesses..."
  putStrLn $ "Guesses : " ++ show guesses  
  --putStrLn $ "-------------------------------------------------"
  putStrLn $ "Average guesses: " ++ show guessAverage


shuffle :: StdGen -> [a] -> [a]
shuffle g xs = shuffle' (randoms g) xs
shuffle' :: [Int] -> [a] -> [a]
shuffle' (i:is) xs = let (firsts, rest) = splitAt (i `mod` length xs) xs
                     in (head rest) : shuffle' is (firsts ++ tail rest)