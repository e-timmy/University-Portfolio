---------------------------------------------------------------------------

-- * Section 1: Pitch Definition

-- | Define Pitch as one note followed by one ocatave
data Pitch = Pitch Note Oct 
    deriving Eq
instance Show Pitch where
    show (Pitch n o) = (show n) ++ (show o) 

-- | Define Note as a given letter
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

-- | Define octave as given number
data Oct = One | Two | Three
    deriving Eq
instance Show Oct where
    show One = "1"
    show Two = "2"
    show Three = "3"

---------------------------------------------------------------------------

-- | Validate character to note transformation
validNote :: Char -> Bool
-- validNote c = c `elem` ['A', 'B', 'C', 'D', 'E', 'F', 'G']
validNote c =
    let n = read c
	in c `elem` noteTypes
	
noteTypes = [A, B, C, D, E, F, G]
octaveTypes = [One, Two, Three]
chordLength = 3