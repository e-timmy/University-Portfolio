
-- [1,2,3,4]
-- [[1,2], [1,3], [1,4], [2,3], [2,4], [3,4]]
-- combinations 2 [1,2,3,4]
-- map (1:) (combinations (n-1)) ++ combinations(xs (n-1)

combinations :: Int -> [a] -> [[a]]
combinations 0 _ = [[]]
combinations _ [] = []
combinations n (x:xs) =
    let n1 = n-1
    in (map (x:) (combinations n1 xs)) ++ (combinations n xs)

combinations2 :: Int -> [a] -> [[a]]
combinations2 0 _ = [[]]
combinations2 _ [] = []
combinations2 n (x:xs) =
    let xCombos = map (x:) (combinations (n-1) xs)
        rest = combinations n xs
    in xCombos ++ rest